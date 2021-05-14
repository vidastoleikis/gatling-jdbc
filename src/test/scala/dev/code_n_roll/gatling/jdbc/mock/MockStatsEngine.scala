/**
  * Copyright 2011-2017 GatlingCorp (http://gatling.io)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package dev.code_n_roll.gatling.jdbc.mock

import java.util.Date

import io.gatling.commons.stats.Status
import io.gatling.core.session.{GroupBlock, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings
import io.gatling.core.stats.writer._
import akka.actor.ActorRef
import com.typesafe.scalalogging.StrictLogging

class MockStatsEngine extends StatsEngine with StrictLogging {

  var dataWriterMsg: List[DataWriterMessage] = List()

  override def start(): Unit = {}

  override def stop(replyTo: ActorRef, exception: Option[Exception]): Unit = {}

  override def logUserStart(scenario: String, timestamp: Long): Unit = {}

  override def logUserEnd(userMessage: UserEndMessage): Unit = {}

  override def logResponse(
                            scenario: String,
                            groups: List[String],
                            requestName:    String,
                            startTimestamp: Long,
                            endTimestamp:   Long,
                            status:         Status,
                            responseCode:   Option[String],
                            message:        Option[String]
                          ): Unit =
    handle(ResponseMessage(
      scenario,
      groups,
      requestName,
      startTimestamp,
      endTimestamp,
      status,
      None,
      message
    ))

  override def logGroupEnd(scenario: String, groupBlock: GroupBlock, exitTimestamp: Long): Unit =
    handle(GroupMessage(
      scenario,
      groupBlock.groups,
      groupBlock.startTimestamp,
      exitTimestamp,
      groupBlock.cumulatedResponseTime,
      groupBlock.status
    ))

  override def logCrash(scenario: String, groups: List[String], requestName: String, error: String): Unit =
    handle(ErrorMessage(error, new Date().getTime))

  override def reportUnbuildableRequest(scenario: String, groups: List[String], requestName: String, errorMessage: String): Unit = {}

  private def handle(msg: DataWriterMessage) = {
    dataWriterMsg = msg :: dataWriterMsg
    logger.info(msg.toString)
  }
}
