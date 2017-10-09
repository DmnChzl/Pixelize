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

package substratum.theme.template

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast

import com.github.javiersantos.piracychecker.PiracyChecker
import com.github.javiersantos.piracychecker.PiracyCheckerUtils
import com.github.javiersantos.piracychecker.enums.InstallerID
import com.github.javiersantos.piracychecker.enums.PiracyCheckerCallback
import com.github.javiersantos.piracychecker.enums.PiracyCheckerError
import com.github.javiersantos.piracychecker.enums.PirateApp

import substratum.theme.template.ThemerConstants.APK_SIGNATURE_PRODUCTION
import substratum.theme.template.ThemerConstants.BASE_64_LICENSE_KEY
import substratum.theme.template.ThemerConstants.ENABLE_KNOWN_THIRD_PARTY_THEME_MANAGERS
import substratum.theme.template.ThemerConstants.ENFORCE_AMAZON_APP_STORE_INSTALL
import substratum.theme.template.ThemerConstants.ENFORCE_GOOGLE_PLAY_INSTALL
import substratum.theme.template.ThemerConstants.ENFORCE_INTERNET_CHECK
import substratum.theme.template.ThemerConstants.ENFORCE_MINIMUM_SUBSTRATUM_VERSION
import substratum.theme.template.ThemerConstants.MINIMUM_SUBSTRATUM_VERSION
import substratum.theme.template.ThemerConstants.PIRACY_CHECK
import substratum.theme.template.ThemerConstants.SUBSTRATUM_FILTER_CHECK
import substratum.theme.template.ThemerConstants.THEME_READY_GOOGLE_APPS
import substratum.theme.template.internal.SystemInformation.SUBSTRATUM_PACKAGE_NAME
import substratum.theme.template.internal.SystemInformation.checkNetworkConnection
import substratum.theme.template.internal.SystemInformation.getSelfSignature
import substratum.theme.template.internal.SystemInformation.getSelfVerifiedIntentResponse
import substratum.theme.template.internal.SystemInformation.getSelfVerifiedPirateTools
import substratum.theme.template.internal.SystemInformation.getSelfVerifiedThemeEngines
import substratum.theme.template.internal.SystemInformation.getSubstratumUpdatedResponse
import substratum.theme.template.internal.SystemInformation.hasOtherThemeSystem
import substratum.theme.template.internal.SystemInformation.isCallingPackageAllowed
import substratum.theme.template.internal.SystemInformation.isPackageInstalled
import substratum.theme.template.internal.TBOConstants.THEME_READY_PACKAGES

import java.io.File
import java.util.*

class SubstratumLauncher : Activity() {

    private var mVerified: Boolean? = false
    private var mPiracyChecker: PiracyChecker? = null
    private var mModeLaunch: String? = ""

    private fun calibrateSystem() {
        if (PIRACY_CHECK && !BuildConfig.DEBUG) {
            startAntiPiracyCheck()
        } else {
            quitSelf()
        }
    }

    private fun startAntiPiracyCheck() {
        if (mPiracyChecker != null) {
            mPiracyChecker!!.start()
        } else {
            if (PIRACY_CHECK && APK_SIGNATURE_PRODUCTION.isEmpty() && !BuildConfig.DEBUG) {
                Log.e("SubstratumAntiPiracyLog", PiracyCheckerUtils.getAPKSignature(this))
            }

            mPiracyChecker = PiracyChecker(this)
            if (ENFORCE_GOOGLE_PLAY_INSTALL) {
                mPiracyChecker!!.enableInstallerId(InstallerID.GOOGLE_PLAY)
            }
            if (ENFORCE_AMAZON_APP_STORE_INSTALL) {
                mPiracyChecker!!.enableInstallerId(InstallerID.AMAZON_APP_STORE)
            }

            mPiracyChecker!!.callback(object : PiracyCheckerCallback() {
                override fun allow() {
                    quitSelf()
                }

                override fun dontAllow(error: PiracyCheckerError, pirateApp: PirateApp?) {
                    val mParse = String.format(
                            getString(R.string.toast_unlicensed),
                            getString(R.string.ThemeName))
                    Toast.makeText(this@SubstratumLauncher, mParse, Toast.LENGTH_SHORT).show()
                    finish()
                }
            })

            if (BASE_64_LICENSE_KEY.isNotEmpty()) {
                mPiracyChecker!!.enableGooglePlayLicensing(BASE_64_LICENSE_KEY)
            }
            if (APK_SIGNATURE_PRODUCTION.isNotEmpty()) {
                mPiracyChecker!!.enableSigningCertificate(APK_SIGNATURE_PRODUCTION)
            }

            mPiracyChecker!!.start()
        }
    }

    private fun getSubstratumFromPlayStore() {
        val mPlayUrl = "https://play.google.com/store/apps/details?id=projekt.substratum"
        val mIntent = Intent(Intent.ACTION_VIEW)
        Toast.makeText(this, getString(R.string.toast_substratum), Toast.LENGTH_SHORT).show()
        mIntent.data = Uri.parse(mPlayUrl)
        startActivity(mIntent)
        finish()
    }

    private fun quitSelf(): Boolean {
        if (!hasOtherThemeSystem(this)) {
            if (!isPackageInstalled(applicationContext, SUBSTRATUM_PACKAGE_NAME)) {
                getSubstratumFromPlayStore()
                return false
            }

            if (ENFORCE_MINIMUM_SUBSTRATUM_VERSION && !getSubstratumUpdatedResponse(applicationContext)) {
                val mParse = String.format(
                        getString(R.string.outdated_substratum),
                        getString(R.string.ThemeName),
                        MINIMUM_SUBSTRATUM_VERSION.toString())
                Toast.makeText(this, mParse, Toast.LENGTH_SHORT).show()
                return false
            }
        } else if (!ENABLE_KNOWN_THIRD_PARTY_THEME_MANAGERS) {
            Toast.makeText(this, R.string.unauthorized_theme_client, Toast.LENGTH_LONG).show()
            finish()
            return false
        }

        var mIntent = Intent()
        if (intent.action == "projekt.substratum.GET_KEYS") {
            mIntent = Intent("projekt.substratum.RECEIVE_KEYS")
        }

        val mThemeName = getString(R.string.ThemeName)
        val mThemeAuthor = getString(R.string.ThemeAuthor)
        val mThemePid = packageName
        val mThemeMode = mModeLaunch
        mIntent.putExtra("theme_name", mThemeName)
        mIntent.putExtra("theme_author", mThemeAuthor)
        mIntent.putExtra("theme_pid", mThemePid)
        mIntent.putExtra("theme_mode", mThemeMode)

        val mThemeHash = getSelfSignature(applicationContext)
        val mThemeLaunchType = getSelfVerifiedThemeEngines(applicationContext)
        val mThemePiracyCheck = getSelfVerifiedPirateTools(applicationContext)
        if (!mThemePiracyCheck || SUBSTRATUM_FILTER_CHECK && (!mVerified!!)) {
            Toast.makeText(this, R.string.unauthorized, Toast.LENGTH_LONG).show()
            finish()
            return false
        }

        mIntent.putExtra("theme_hash", mThemeHash)
        mIntent.putExtra("theme_launch_type", mThemeLaunchType)
        mIntent.putExtra("theme_debug", BuildConfig.DEBUG)
        mIntent.putExtra("theme_piracy_check", mThemePiracyCheck)
        mIntent.putExtra("encryption_key", BuildConfig.DECRYPTION_KEY)
        mIntent.putExtra("iv_encrypt_key", BuildConfig.IV_KEY)

        if (intent.action == "projekt.substratum.THEME") {
            setResult(getSelfVerifiedIntentResponse(applicationContext)!!, mIntent)
        } else if (intent.action == "projekt.substratum.GET_KEYS") {
            val mPackage = intent.getStringExtra("calling_package_name")
            mIntent.`package` = mPackage
            mIntent.action = "projekt.substratum.RECEIVE_KEYS"
            if (mPackage != null) {
                if (isCallingPackageAllowed(mPackage)) {
                    sendBroadcast(mIntent)
                }
            }
        }

        finish()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mIntent = intent
        mVerified = mIntent.getBooleanExtra("certified", false)
        mModeLaunch = mIntent.getStringExtra("theme_mode")

        val mSharedPref = getPreferences(Context.MODE_PRIVATE)
        if (ENFORCE_INTERNET_CHECK) {
            if (mSharedPref.getInt("last_version", 0) == BuildConfig.VERSION_CODE) {
                if (THEME_READY_GOOGLE_APPS) {
                    detectThemeReady()
                } else {
                    calibrateSystem()
                }
            } else {
                checkConnection()
            }
        } else if (THEME_READY_GOOGLE_APPS) {
            detectThemeReady()
        } else {
            calibrateSystem()
        }
    }

    private fun checkConnection(): Boolean {
        val isConnected = checkNetworkConnection()
        if (!isConnected!!) {
            Toast.makeText(this, R.string.toast_internet, Toast.LENGTH_LONG).show()
            return false
        } else {
            val mEditor = getPreferences(Context.MODE_PRIVATE).edit()
            mEditor.putInt("last_version", BuildConfig.VERSION_CODE).apply()
            if (THEME_READY_GOOGLE_APPS) {
                detectThemeReady()
            } else {
                calibrateSystem()
            }
            return true
        }
    }

    private fun detectThemeReady() {
        val mAddon = File("/system/addon.d/80-ThemeReady.sh")
        if (mAddon.exists()) {
            val mApps = ArrayList<String>()
            var mUpdated = false
            var mIncomplete = false
            val mPackageManager = this.packageManager
            val mAppName = StringBuilder()

            if (!mIncomplete) {
                for (packageName in THEME_READY_PACKAGES) {
                    try {
                        val mAppInfo = mPackageManager.getApplicationInfo(packageName, 0)
                        if (mAppInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0) {
                            mUpdated = true
                            mApps.add(mPackageManager.getApplicationLabel(mAppInfo).toString())
                        }
                    } catch (e: Exception) {
                        // Package Not Found
                    }
                }
            }

            for (i in mApps.indices) {
                mAppName.append(mApps[i])
                if (i <= mApps.size - 3) {
                    mAppName.append(", ")
                } else if (i == mApps.size - 2) {
                    mAppName.append(" ").append(getString(R.string.and)).append(" ")
                }
            }

            if (!mUpdated && !mIncomplete) {
                calibrateSystem()
            } else {
                val mInteger = if (mIncomplete) {
                    R.string.theme_ready_incomplete
                } else {
                    R.string.theme_ready_updated
                }

                val mParse = String.format(getString(mInteger), mAppName)

                AlertDialog.Builder(this, R.style.DialogStyle)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(getString(R.string.ThemeName))
                        .setMessage(mParse)
                        .setPositiveButton(R.string.yes) { _, _ -> calibrateSystem() }
                        .setNegativeButton(R.string.no) { _, _ -> finish() }
                        .setOnCancelListener { finish() }
                        .show()
            }
        } else {
            AlertDialog.Builder(this, R.style.DialogStyle)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(getString(R.string.ThemeName))
                    .setMessage(getString(R.string.theme_ready_not_detected))
                    .setPositiveButton(R.string.yes) { _, _ -> calibrateSystem() }
                    .setNegativeButton(R.string.no) { _, _ -> finish() }
                    .setOnCancelListener { finish() }
                    .show()
        }
    }
}
