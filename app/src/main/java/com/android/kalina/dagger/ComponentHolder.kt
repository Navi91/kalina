package com.android.kalina.dagger

import com.android.kalina.app.Studio21Application

class ComponentHolder {

    companion object {
        private lateinit var applicationComponent: ApplicationComponent

        fun initApplication(application: Studio21Application) {
            applicationComponent = DaggerApplicationComponent.builder().applicationModule(ApplicationModule(application)).build()
        }

        fun applicationComponent() = applicationComponent
    }
}