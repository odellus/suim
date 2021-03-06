/*
 *  Copyright 2013 Carnegie Mellon University
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package edu.cmu.lti.suim.examples

import scala.collection.JavaConversions.collectionAsScalaIterable

import org.apache.spark.SparkContext
import org.apache.uima.examples.cpe.FileSystemCollectionReader
import org.apache.uima.fit.factory.AnalysisEngineFactory
import org.apache.uima.fit.factory.CollectionReaderFactory
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory
import org.apache.uima.fit.util.JCasUtil
import org.apache.uima.tutorial.RoomNumber
import org.apache.uima.tutorial.ex1.RoomNumberAnnotator

import edu.cmu.lti.suim.SparkUimaUtils.process
import edu.cmu.lti.suim.SparkUimaUtils.sequenceFile


object AppWithHDFS {

  def main(args: Array[String]) = {
    val sc = new SparkContext(args(0), "App",
      System.getenv("SPARK_HOME"), System.getenv("SPARK_CLASSPATH").split(":"))

    val typeSystem = TypeSystemDescriptionFactory.createTypeSystemDescription()
    val params = Seq(FileSystemCollectionReader.PARAM_INPUTDIR, "data")
    val rdd = sequenceFile(CollectionReaderFactory.createCollectionReader(classOf[FileSystemCollectionReader], params: _*),
      "hdfs://localhost:9000/file.txt",sc)
    val rnum = AnalysisEngineFactory.createEngineDescription(classOf[RoomNumberAnnotator])
    val rooms = rdd.map(process(_, rnum)).flatMap(scas => JCasUtil.select(scas.jcas, classOf[RoomNumber]))
    val counts = rooms.map(room => room.getBuilding()).countByValue()
    println(counts)
  }
}
