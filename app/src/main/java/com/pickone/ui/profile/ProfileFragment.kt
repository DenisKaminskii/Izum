package com.pickone.ui.profile

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.pickone.R
import com.pickone.browser.StandaloneBrowser
import com.pickone.data.DeviceIdProvider
import com.pickone.data.DeviceInfoProvider
import com.pickone.data.repository.UserRepository
import com.pickone.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

// § Rename to SettingsFragment
@AndroidEntryPoint
class ProfileFragment : Fragment(), CoroutineScope by MainScope() {

    companion object {

        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }

    }

    @Inject lateinit var deviceInfoProvider: DeviceInfoProvider
    @Inject lateinit var deviceIdProvider: DeviceIdProvider
    @Inject lateinit var userRepository: UserRepository

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vgPrivacy.setOnClickListener{ onPrivacyClicked() }
        binding.vgTerms.setOnClickListener{ onTermsClicked() }
        binding.tvContactUsAction.setOnClickListener { onSendFeedbackClicked() }
    }

    private fun onPrivacyClicked() {
        StandaloneBrowser.open(requireContext(), getString(R.string.privacy_policy))
    }

    private fun onTermsClicked() {
        StandaloneBrowser.open(requireContext(), getString(R.string.terms_use))
    }

    private fun onSendFeedbackClicked() {
        sendEmail(
            context = requireContext(),
            email = "polleo.corp@gmail.com"
        )
    }

    private fun sendEmail(
        context: Context,
        email: String,
        subject: String? = null,
        body: String? = null
    ) = launch(Dispatchers.Unconfined) {
        val mailIntent = getEmailIntent(context, email, subject, body)
        val gmailIntent = Intent(mailIntent)
        gmailIntent.addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_NEW_TASK)
        gmailIntent.setPackage("com.google.android.gm")
        tryStartEmail(
            action = { context.startActivity(gmailIntent) },
            onFailed = {
                tryStartEmail(
                    action = {
                        val intentTitle = "Send email"
                        context.startActivity(Intent.createChooser(mailIntent, intentTitle))
                    },
                    onFailed = {
                        Toast.makeText(
                            context,
                            "There are no any email clients installed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        )
    }

    private fun tryStartEmail(action: () -> Unit, onFailed: () -> Unit) {
        try {
            action()
        } catch (_: ActivityNotFoundException) {
            onFailed()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun getEmailIntent(
        context: Context,
        emailAddress: String,
        emailSubject: String?,
        customBody: String?,
    ): Intent {
        val versionName = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (_: Exception) {
            "1.0"
        }
        val deviceName = deviceInfoProvider.deviceName
        val androidVersion = deviceInfoProvider.osVersion
        val language = deviceInfoProvider.language
        val deviceId = deviceIdProvider.deviceId
        val userId = userRepository.userId
        val subject = "PickOne Feedback"

        val defaultBody = """
            [Diagnostic information]
            App version: $versionName
            Device name: $deviceName
            Android version: $androidVersion
            Language: $language
            Device ID: $deviceId
            User ID: $userId
            ------------------------------
        """.trimIndent() + "\n"


        val text = if (!customBody.isNullOrEmpty()) {
            "${customBody}\n$defaultBody"
        } else {
            defaultBody
        }
        val email = Intent()
        email.type = "message/rfc822"
        email.putExtra(Intent.EXTRA_SUBJECT, subject)
        email.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
        email.putExtra(Intent.EXTRA_TEXT, text)
        email.addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_NEW_TASK
        )
        return email
    }

    private fun Context.getUriForFile(file: File): Uri? =
        FileProvider.getUriForFile(
            this,
            applicationContext.packageName + ".provider",
            file
        )

}