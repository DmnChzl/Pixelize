/**
 * Copyright (C) 2017 Damien Chazoule
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package substratum.theme.template.internal

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature

import substratum.theme.template.ThemerConstants.BLACKLISTED_APPLICATIONS
import substratum.theme.template.ThemerConstants.ENABLE_BLACKLISTED_APPLICATIONS
import substratum.theme.template.ThemerConstants.ENABLE_KNOWN_THIRD_PARTY_THEME_MANAGERS
import substratum.theme.template.ThemerConstants.MINIMUM_SUBSTRATUM_VERSION
import substratum.theme.template.ThemerConstants.OTHER_THEME_SYSTEMS

object SystemInformation {

    val SUBSTRATUM_PACKAGE_NAME = "projekt.substratum"

    fun checkNetworkConnection(): Boolean? {
        var isConnected = false
        try {
            val mProcess = Runtime.getRuntime().exec("/system/bin/ping -c 1 www.google.com")
            val mValue = mProcess.waitFor()
            isConnected = mValue == 0
        } catch (e: Exception) {
            // Suppress Error
        }

        return isConnected
    }

    fun isPackageInstalled(context: Context, package_name: String): Boolean {
        try {
            val mPackageManager = context.packageManager
            val mAppInfo = context.packageManager.getApplicationInfo(package_name, 0)
            mPackageManager.getPackageInfo(package_name, PackageManager.GET_ACTIVITIES)
            return mAppInfo.enabled
        } catch (e: Exception) {
            return false
        }
    }

    fun hasOtherThemeSystem(context: Context): Boolean {
        try {
            val mPackageManager = context.packageManager
            for (info: String in OTHER_THEME_SYSTEMS) {
                val mAppInfo = mPackageManager.getApplicationInfo(info, 0)
                mPackageManager.getPackageInfo(info, PackageManager.GET_ACTIVITIES)
                return mAppInfo.enabled
            }
        } catch (e: Exception) {
            // Suppress Error
        }

        return false
    }

    fun getSubstratumUpdatedResponse(context: Context): Boolean {
        try {
            val mPackageInfo = context.applicationContext.packageManager
                    .getPackageInfo(SUBSTRATUM_PACKAGE_NAME, 0)
            if (mPackageInfo.versionCode >= MINIMUM_SUBSTRATUM_VERSION) {
                return true
            }
        } catch (e: Exception) {
            // Suppress Warning
        }

        return false
    }

    fun getSelfVerifiedIntentResponse(context: Context): Int? {
        if (ENABLE_KNOWN_THIRD_PARTY_THEME_MANAGERS) {
            return getSelfSignature(context)
        } else {
            return getSubstratumSignature(context)
        }
    }

    fun getSelfVerifiedPirateTools(context: Context): Boolean {
        if (ENABLE_BLACKLISTED_APPLICATIONS) {
            BLACKLISTED_APPLICATIONS
                    .filter { isPackageInstalled(context, it) }
                    .forEach { return false }
        }

        return true
    }

    fun getSelfVerifiedThemeEngines(context: Context): Boolean? {
        val isPermitted: Boolean? = OTHER_THEME_SYSTEMS.any { isPackageInstalled(context, it) }
        if (ENABLE_KNOWN_THIRD_PARTY_THEME_MANAGERS) {
            return isPermitted
        } else if (isPackageInstalled(context, SUBSTRATUM_PACKAGE_NAME)) {
            return (!isPermitted!!)
        }

        return false
    }

    fun isCallingPackageAllowed(packageId: String): Boolean {
        if (packageId == SUBSTRATUM_PACKAGE_NAME) return true
        if (ENABLE_KNOWN_THIRD_PARTY_THEME_MANAGERS) {
            OTHER_THEME_SYSTEMS.filter { packageId == it }.forEach { return true }
        }

        return false
    }

    @SuppressLint("PackageManagerGetSignatures")
    private fun getSubstratumSignature(context: Context): Int {
        val mSignatures: Array<Signature>
        try {
            mSignatures = context.packageManager.getPackageInfo(
                    SUBSTRATUM_PACKAGE_NAME,
                    PackageManager.GET_SIGNATURES
            ).signatures
            return mSignatures[0].hashCode()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return 0
    }

    @SuppressLint("PackageManagerGetSignatures")
    fun getSelfSignature(context: Context): Int {
        val mSignatures: Array<Signature>
        try {
            mSignatures = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
            ).signatures
            return mSignatures[0].hashCode()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return 0
    }
}
