package com.android.kalina.dagger

import com.android.kalina.app.KalinaApplication

class ComponentHolder {

    companion object {
        private lateinit var applicationComponent: ApplicationComponent

        fun initApplication(application: KalinaApplication) {
            applicationComponent = DaggerApplicationComponent.builder().applicationModule(ApplicationModule(application)).build()
        }

        fun applicationComponent() = applicationComponent
    }
}