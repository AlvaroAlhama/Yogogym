package yogogym

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class HU21_ParticipateChallenge extends Simulation {

	val httpProtocol = http
		.baseUrl("http://www.yogogym.com")
		.inferHtmlResources(BlackList(""".*.css""", """.*.js""", """.*.ico""", """.*.png""", """.*.jpg""", """.*.jpeg""", """.*.woff""", """.+.woff2"""), WhiteList())
		.acceptEncodingHeader("gzip, deflate")
		.userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:76.0) Gecko/20100101 Firefox/76.0")

	val headers_0 = Map(
		"Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
		"Accept-Language" -> "es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3",
		"Upgrade-Insecure-Requests" -> "1")

	val headers_2 = Map(
		"Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
		"Accept-Language" -> "es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3",
		"Origin" -> "http://www.yogogym.com",
		"Upgrade-Insecure-Requests" -> "1")



	object Home {
		val home = exec(http("Home")
			.get("/")
			.headers(headers_0))
		.pause(4)
	}
	
	object Login{
		val loginClient = exec(http("Login Client")
			.get("/login")
			.headers(headers_0)
			.check(css("input[name=_csrf]", "value").saveAs("stoken"))
		).pause(9)
		.exec(http("Logged Client")
			.post("/login")
			.headers(headers_2)
			.formParam("username", "client1")
			.formParam("password", "client1999")
			.formParam("_csrf", "${stoken}")
		).pause(13)

		val loginAdmin = exec(http("Login Admin")
			.get("/login")
			.headers(headers_0)
			.check(css("input[name=_csrf]", "value").saveAs("stoken"))
		).pause(9)
		.exec(http("Logged Admin")
			.post("/login")
			.headers(headers_2)
			.formParam("username", "admin1")
			.formParam("password", "admin1999")
			.formParam("_csrf", "${stoken}")
		).pause(13)
	}

	object ListNewChallenges{
		val listNewChallenges = exec(http("List New Challenges")
			.get("/client/client1/challenges")
			.headers(headers_0))
		.pause(10)
	}

	object ShowNewChallenge{
		val showNewChallenge = exec(http("Show New Challenge")
			.get("/client/client1/challenges/4")
			.headers(headers_0))
		.pause(9)
	}

	object ListSubmittedChallenges{
		val listSubmittedChallenges = exec(http("List Submitted Challenges")
			.get("/admin/inscriptions/submitted")
			.headers(headers_0))
		.pause(8)
	}

	object ShowSubmittedChallenge{
		val showSubmittedChallenge = exec(http("Show Submitted Challenge")
			.get("/admin/inscriptions/submitted/1")
			.headers(headers_0))
		.pause(8)
	}	
	

	val listAndShowNewChallengeClientScn = scenario("Inscribe And Submit Challenge Client").exec(
																Home.home,
																Login.loginClient,
																ListNewChallenges.listNewChallenges,
																ShowNewChallenge.showNewChallenge)
																
	val listAndShowSubmittedChallengeAdminScn = scenario("Evaluate Challenge Admin").exec(
																Home.home,
																Login.loginAdmin,
																ListSubmittedChallenges.listSubmittedChallenges,
																ShowSubmittedChallenge.showSubmittedChallenge)
	

	setUp(
		listAndShowNewChallengeClientScn.inject(rampUsers(6000) during (100 seconds)),     // 7500, 100
		listAndShowSubmittedChallengeAdminScn.inject(rampUsers(6000) during (100 seconds))  // 7500, 100
		).protocols(httpProtocol)
		 .assertions(
					global.responseTime.max.lt(5000),    
					global.responseTime.mean.lt(1000),
					global.successfulRequests.percent.gt(95)
					)
	
}