package yogogym

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class HU06_DashboardGeneral extends Simulation {

	val httpProtocol = http
		.baseUrl("http://www.yogogym.com")
		.inferHtmlResources(BlackList(""".*.css""", """.*.js""", """.*.ico""", """.*.png""", """.*.jpg""", """.*.jpeg""", """.*.woff""", """.+.woff2"""), WhiteList())
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3")
		.upgradeInsecureRequestsHeader("1")
		.userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:76.0) Gecko/20100101 Firefox/76.0")

	val headers_2 = Map("Origin" -> "http://www.yogogym.com")


	object Home {
		val home = exec(http("Home")
			.get("/"))
		.pause(8)
	}

	object Login{
		val login = exec(http("Login")
			.get("/login")
			.check(css("input[name=_csrf]", "value").saveAs("stoken"))
		).pause(10)
		.exec(http("Logged")
			.post("/login")
			.headers(headers_2)
			.formParam("username", "admin1")
			.formParam("password", "admin1999")
			.formParam("_csrf", "${stoken}")
		).pause(15)
	}

	object ShowDashboardGeneral{
		val showDashboardGeneral = exec(http("Show Dashboard General")
			.get("/admin/dashboardGeneral"))
		.pause(13)
	}

	val showDashboardGeneralScn = scenario("Show Dashboard General").exec(
																Home.home,
																Login.login,
																ShowDashboardGeneral.showDashboardGeneral)

	setUp(
		showDashboardGeneralScn.inject(rampUsers(8000) during (90 seconds))    // 7000, 100
		).protocols(httpProtocol)
		 .assertions(
					global.responseTime.max.lt(5000),    
					global.responseTime.mean.lt(1000),
					global.successfulRequests.percent.gt(95)
					)
}