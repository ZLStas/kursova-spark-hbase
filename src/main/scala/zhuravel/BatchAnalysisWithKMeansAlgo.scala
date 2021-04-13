package zhuravel

import org.apache.spark.ml.clustering.{KMeans, KMeansModel}
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.sql.functions.udf
import org.spark_project.guava.io.Resources.getResource

import java.util.UUID

/** The main class */
object BatchAnalysisWithKMeansAlgo {

  /** Main function */
  def main(args: Array[String]): Unit = {
    // context for spark
    val spark = SparkSession.builder
      .master("local[*]")
      .appName("lambda")
      .getOrCreate()

//     SparkSession has implicits
    import spark.implicits._

    // schema
    val schema: StructType = StructType(
      StructField("createdAt", TimestampType, nullable = false) ::
        StructField("createdBy", StringType, nullable = false) ::
        StructField("value", IntegerType, nullable = false) ::
        Nil
    )
    // read to DataFrame
    val uberDf = spark.read.format("csv")
      .option("header", value = true)
      .option("delimiter", ",")
      .option("timestampFormat", "yyyy-MM-dd HH:mm:ss")
      .schema(schema)
      .load(getResource("data.csv").getPath)
      .cache()
    uberDf.printSchema()
    uberDf.show(10)

    // transform userDf with VectorAssembler to add feature column
    val cols = Array("value")
    val assembler = new VectorAssembler().setInputCols(cols).setOutputCol("features")
    val featureDf = assembler.transform(uberDf)
    featureDf.printSchema()
    featureDf.show(10)

    // split data set training(70%) and test(30%)
    val seed = 5043
    val Array(trainingData, testData) = featureDf.randomSplit(Array(0.7, 0.3), seed)

    // kmeans model with 3 clusters
    val kmeans = new KMeans()
      .setK(3)
      .setFeaturesCol("features")
      .setPredictionCol("prediction")
    val kmeansModel = kmeans.fit(trainingData)
    kmeansModel.clusterCenters.foreach(println)

    // test the model with test data set
    val predictDf = kmeansModel.transform(testData)
    predictDf.show(10)

    // calculate distance from center
    val distFromCenter = udf((features: Vector, c: Int) => Vectors.sqdist(features, kmeansModel.clusterCenters(c)))
    val distanceDf = predictDf.withColumn("distance", distFromCenter($"features", $"prediction"))
    distanceDf.show(10)

    // no of categories
    predictDf.groupBy("prediction").count().show()

    // save model
    kmeansModel.write.overwrite()
      .save("/home/stanslav/Desktop/Diplome/kursova-spark-hbase/src/main/resources/models")

    // load model
    val kmeansModelLoded = KMeansModel
      .load("/home/stanslav/Desktop/Diplome/kursova-spark-hbase/src/main/resources/models")

    // sample data, it could comes via kafka(through spark streams)
    val df1 = Seq(
      ("2020-07-24 04:22:01", "e9834a76-ada7-469e-a2be-43db255d47d4", 24),
      ("2020-05-21 19:20:16", "e36f14bf-f469-44ef-ada5-5f26887b3e92", 15),
      ("2020-09-09 17:41:21", "93548b94-5888-4f45-9177-16243e755ee9", 15),
      ("2021-01-18 04:31:00", "e36f14bf-f469-44ef-ada5-5f26887b3e92", 24),
      ("2020-07-05 18:03:08", "e9859165-1ce2-40bc-85fe-f503a182ec9b", 41)
    ).toDF("createdAt", "createdBy", "value")
    df1.show()

    // transform sample data set and add feature column
    val df2 = assembler.transform(df1)
    df2.show()

    // prediction of sample data set with loaded model
    val df3 = kmeansModelLoded.transform(df2)
    df3.show()

  }
}
