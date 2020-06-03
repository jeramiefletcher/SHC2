package com.hpe.hbase

/**
  * Created by pratap on 12/9/19.
  * To Read and write through Dataframe using simple parameter pass from code
  */

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.execution.datasources.hbase.{HBaseRelation, HBaseTableCatalog}

object HbaseManager  {

  private def getCatalog(columns: String, namespace: String,hbtable: String, cf:String,read_type:Boolean): String =
  {
    var cat =""
    if (read_type == true) {
      cat =
        s"""{
       "table":{"namespace":"2", "name":"0"},
       "rowkey":"rkey",
       "timestamp":"ts",
       "columns":{
       "rkey":{"cf":"rowkey", "col":"rkey", "type":"string"},
       "ts":{"cf":"timestamp", "col":"ts", "type":"long"},
        8
       }
       }""".stripMargin
    }
    else
      {
        cat =
          s"""{
       "table":{"namespace":"2", "name":"0"},
       "rowkey":"rkey",
       "columns":{
       "rkey":{"cf":"rowkey", "col":"rkey", "type":"string"},
        8
       }
       }""".stripMargin
      }
    var str: StringBuilder = new StringBuilder

    if (columns != null && columns.length == 0) {
      println("No columns has been selected to created Catalog")
      //System.exit(-1)
    }
    var colArray = columns.split(",")
    var j = 0
    while (j < colArray.length) {
      var colstr = '"' + colArray(j) + '"' + ":{" + '"' + "cf" + '"' + ":" + '"' + cf + '"' + ", " + '"' + "col" + '"' + ":" + '"' + colArray(j) + '"' + ", " + '"' + "type" + '"' + ":" + '"' + "String" + '"' + "}"
      str.append(colstr)

      if (j != colArray.length - 1) {str.append(",")}
      j = j + 1
    }


    var nameSpacePattern = "[2-3]".r
    var resNameSpace = nameSpacePattern.replaceAllIn(cat, namespace)
    var numPattern = "[0-1]".r
    //val res1 = numPattern.replaceAllIn(resNameSpace, s"${namespace}:${hbtable}")
    var res1 = numPattern.replaceAllIn(resNameSpace, s"${hbtable}")
    var numberPattern = "[8-9]".r
    var res = ""
    res = numberPattern.replaceAllIn(res1, str.toString())
    return res
  }

  /**
   * XD{y35bddiMOvs@b
   * This method fetch the data from Hbase table in dataframe as output
   * @spark Spark Session
   * @param columns
   * @param namespaceV
   * @param tableV
   * @param cf
   * @param maxVersion
   * @param minTimeStamp
   * @param maxTimeStamp
   * @return
   */
  def getDF( spark:SparkSession,
             columns: String,
            namespaceV: String,
            tableV: String,
            cf:String,
            latestVersion:Boolean = false,
            maxVersion:Int=9999,
            minTimeStamp:Long = 0,
            maxTimeStamp:Long = System.currentTimeMillis()): DataFrame = {

    var retDF : DataFrame = null
    var table = tableV
    var namespace : String = null

    if (namespaceV.length() == 0){
      namespace="default"
    }
    else{
      namespace=namespaceV
    }
    val cat = getCatalog(columns,namespace,table,cf,true)

    /*
    val conf = new SparkConf().setAppName("hbaseConnector")
    val builder =  SparkSession.builder()
      .config(conf)
      .enableHiveSupport()
    val spark1 = builder.getOrCreate()
  */
    retDF =  spark.sqlContext.read
      .options(Map(HBaseTableCatalog.tableCatalog->cat,HBaseRelation.MAX_VERSIONS -> maxVersion.toString,
        HBaseRelation.MIN_STAMP->minTimeStamp.toString,HBaseRelation.MAX_STAMP->maxTimeStamp.toString,
        HBaseRelation.MERGE_TO_LATEST -> latestVersion.toString))
      .format("org.apache.spark.sql.execution.datasources.hbase")
      .load()

    return retDF
  }


  /**
   * GetDF for pyspark and without sparksession
   * @param columns
   * @param namespaceV
   * @param tableV
   * @param cf
   * @param latestVersion
   * @return
   */
  def  getDF(
             columns: String,
             namespaceV: String,
             tableV: String,
             cf:String,
             latestVersion:Boolean): DataFrame = {


    var retDF : DataFrame = null
    val conf = new SparkConf().setAppName("hbaseConnector")
    val builder =  SparkSession.builder()
      .config(conf)
      .enableHiveSupport()
    val spark1 = builder.getOrCreate()

    //get DF
    retDF =  getDF(spark1,columns,namespaceV,tableV,cf,latestVersion)
    return retDF
  }

  def  getDF(
              columns: String,
              namespaceV: String,
              tableV: String,
              cf:String): DataFrame = {


    var retDF : DataFrame = null
    val conf = new SparkConf().setAppName("hbaseConnector")
    val builder =  SparkSession.builder()
      .config(conf)
      .enableHiveSupport()
    val spark1 = builder.getOrCreate()

    //get DF
    retDF =  getDF(spark1,columns,namespaceV,tableV,cf,false)
    return retDF
  }

  def  getDF(
              columns: String,
              namespaceV: String,
              tableV: String,
              cf:String,
              latestVersion:Boolean,
              maxVersionV:Int,
              minTimeStampV:Long,
              maxTimeStampV:Long ): DataFrame = {



    var retDF : DataFrame = null
    val conf = new SparkConf().setAppName("hbaseConnector")
    val builder =  SparkSession.builder()
      .config(conf)
      .enableHiveSupport()
    val spark1 = builder.getOrCreate()

    var maxVersion:Int=9999
    if(maxVersionV > 0){
      maxVersion = maxVersionV
    }
    var minTimeStamp:Long = 0
    if(minTimeStampV > 0){
      minTimeStamp = minTimeStampV
    }
    var maxTimeStamp:Long = System.currentTimeMillis()
    if(maxTimeStampV > 0){
      maxTimeStamp = maxTimeStampV
    }

    //get DF
    retDF =  getDF(spark1,columns,namespaceV,tableV,cf,latestVersion,maxVersion,minTimeStamp,maxTimeStamp)
    return retDF
  }
  /**
   * This method help to store the Dataframe to Hbase Tables
   * @param df
   * @param columns
   * @param pkcolumns
   * @param namespaceV
   * @param tableV
   * @param cf
   */
  def setDF(df: DataFrame, columns:String,pkcolumns:String,
            namespaceV: String,tableV: String, cf: String): Unit = {

    var table = tableV
    var namespace: String = namespaceV
    if (namespaceV.length() == 0) {
      namespace = "default"
    }
    val cat = getCatalog(columns, namespace, table,cf, false)
    var needed_column = columns.split(",").toList.toSeq
    var required_columns = pkcolumns.split(",").toList.toSeq
    // filter the columns based on Hbase schema
    val DF_out:DataFrame = df.select(needed_column.head,needed_column.tail:_*)

    //var FinalDF=DF_out.selectExpr("concat("+ needed_column.mkString(",") + ") as rkey ,"+  needed_column.mkString(",") )
    //DF_out.withColumn("rkey",concat(col()))
    import org.apache.spark.sql.functions.expr
    // row key column key added based on required column for Hbase
    val hbaseFinalDF=DF_out.withColumn("rkey",expr("concat("+ required_columns.mkString(",") + ")" ))

    // store the bulk load to Hbase
    hbaseFinalDF.write
      .options(Map(HBaseTableCatalog.tableCatalog -> cat, HBaseTableCatalog.tableName -> "5"))
      .format("org.apache.spark.sql.execution.datasources.hbase")
      .save()

  }



}
