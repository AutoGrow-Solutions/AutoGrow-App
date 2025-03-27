package com.example.smarthydro.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthydro.repositories.ComponentRepository
import kotlinx.coroutines.launch

class ComponentViewModel : ViewModel() {
    private val repository = ComponentRepository()

    fun setLight() {
        viewModelScope.launch {
            try {
                repository.toggleLight()
            } catch (e: Exception) {
                Log.e("LIGHT ERROR", e.message.toString())
            }
            //the second base url's call is set here
            try {

                repository.toggleLight2()
            } catch (e: Exception) {
                Log.e("LIGHT ERROR", e.message.toString())
            }
        }
    }

    fun setPump() {
        viewModelScope.launch {
            try {
                repository.togglePump()
            } catch (e: Exception) {
                Log.e("PUMP ERROR", e.message.toString())
            }
            //the second base url's call is set here
            try {

                repository.togglePump2()
            } catch (e: Exception) {
                Log.e("PUMP ERROR", e.message.toString())
            }
        }
    }

    fun setExtractor() {
        viewModelScope.launch {
            try {
                repository.toggleExtractor()
            } catch (e: Exception) {
                Log.e("EXTRACTOR ERROR", e.message.toString())
            }
            //the second base url's call is set here
            try {

                repository.toggleExtractor2()
            } catch (e: Exception) {
                Log.e("EXTRACTOR ERROR", e.message.toString())
            }
        }
    }
    fun setFan() {
        viewModelScope.launch {
            try {
                repository.toggleFan()

            } catch (e: Exception) {
                Log.e("FAN ERROR", e.message.toString())
            }
            //the second base url's call is set here
            try {

                repository.toggleFan2()
            } catch (e: Exception) {
                Log.e("FAN ERROR", e.message.toString())
            }
        }
    }

    fun setPh() {
        viewModelScope.launch {
            try {
                repository.ph()
            } catch (e: Exception) {
                Log.e("PH ERROR", e.message.toString())
            }
            //the second base url's call is set here
            try {
                repository.ph2()
            } catch (e: Exception) {
                Log.e("PH ERROR", e.message.toString())
            }
        }
    }

    fun setPhUp() {
        viewModelScope.launch {
            try {
                repository.phUp()
            } catch (e: Exception) {
                Log.e("PH ERROR", e.message.toString())
            }

//the second base url's call is set here
            try {
                repository.phUp2()
            } catch (e: Exception) {
                Log.e("PH ERROR", e.message.toString())
            }
        }
    }

    fun setPhDown() {
        viewModelScope.launch {
            try {
                repository.phDown()
            } catch (e: Exception) {
                Log.e("PH ERROR", e.message.toString())
            }
            //the second base url's call is set here
            try {
                repository.phDown2()
            } catch (e: Exception) {
                Log.e("PH ERROR", e.message.toString())
            }
        }
    }

    fun setEc() {
        viewModelScope.launch {
            try {
                repository.ec()
            } catch (e: Exception) {
                Log.e("EC ERROR", e.message.toString())
            }
            //the second base url's call is set here
            try {

                repository.ec2()
            } catch (e: Exception) {
                Log.e("EC ERROR", e.message.toString())
            }
        }
    }

    fun setEcUp() {
        viewModelScope.launch {
            try {
                repository.ecUp()
            } catch (e: Exception) {
                Log.e("EC ERROR", e.message.toString())
            }
            //the second base url's call is set here
            try {

                repository.ecUp2()
            } catch (e: Exception) {
                Log.e("EC ERROR", e.message.toString())
            }
        }
    }

    fun setEcDown() {
        viewModelScope.launch {
            try {
                repository.ecDown()
            } catch (e: Exception) {
                Log.e("EC ERROR", e.message.toString())
            }
            //the second base url's call is set here
            try {

                repository.ecDown2()
            } catch (e: Exception) {
                Log.e("EC ERROR", e.message.toString())
            }
        }
    }

}