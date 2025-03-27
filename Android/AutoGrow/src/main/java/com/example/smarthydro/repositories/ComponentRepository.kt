package com.example.smarthydro.repositories

import com.example.smarthydro.services.ComponentService


class ComponentRepository {
    private val componentService = ComponentService.buildService()
    private val componentService2 = ComponentService.buildService2()
    suspend fun toggleLight() {
        return componentService.toggleLight()
    }

    suspend fun togglePump() {
        return componentService.togglePump()
    }

    suspend fun toggleExtractor() {
        return componentService.toggleExtractor()
    }

    suspend fun ec() {
        return componentService.ec()
    }

    suspend fun ecUp() {
        return componentService.ecUp()
    }

    suspend fun ecDown() {
        return componentService.ecDown()
    }

    suspend fun ph() {
        return componentService.pH()
    }

    suspend fun phUp() {
        return componentService.pHUp()
    }

    suspend fun phDown() {
        return componentService.pHDown()
    }

    suspend fun toggleFan() {
        return componentService.toggleFan()
    }

    //The following functions are defined so that the second url can be used here
    suspend fun toggleLight2() {
        return componentService2.toggleLight2()
    }

    suspend fun togglePump2() {
        return componentService2.togglePump2()
    }

    suspend fun toggleExtractor2() {
        return componentService2.toggleExtractor2()
    }

    suspend fun ec2() {
        return componentService2.ec2()
    }

    suspend fun ecUp2() {
        return componentService2.ecUp2()
    }

    suspend fun ecDown2() {
        return componentService2.ecDown2()
    }

    suspend fun ph2() {
        return componentService2.pH2()
    }

    suspend fun phUp2() {
        return componentService2.pHUp2()
    }

    suspend fun phDown2() {
        return componentService2.pHDown2()
    }

    suspend fun toggleFan2() {
        return componentService2.toggleFan2()
    }
}