import com.soywiz.korge.gradle.*

plugins {
	alias(libs.plugins.korge)
}

korge {
	id = "lv.kristianskaneps.rtu.mipamati.game"
	supportBox2d()
// To enable all targets at once

	//targetAll()

// To enable targets based on properties/environment variables
	//targetDefault()

// To selectively enable targets

	targetJvm()
	targetJs()
	//targetDesktop()
	//targetIos()
	//targetAndroidIndirect() // targetAndroidDirect()
}

tasks.withType<JavaCompile> {
    System.setProperty("file.encoding", "UTF-8")
    options.encoding = "UTF-8"
}
