package com.infinum.thimble.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RestrictTo
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.infinum.thimble.R
import com.infinum.thimble.databinding.ThimbleActivityThimbleBinding
import com.infinum.thimble.ui.fragments.GridOverlayFragment
import com.infinum.thimble.ui.fragments.MagnifierOverlayFragment
import com.infinum.thimble.ui.fragments.MockupOverlayFragment
import com.infinum.thimble.models.PermissionRequest
import com.infinum.thimble.models.configuration.ThimbleConfiguration

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class ThimbleActivity : ServiceActivity() {

    private lateinit var binding: ThimbleActivityThimbleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThimbleActivityThimbleBinding.inflate(layoutInflater)
            .also { setContentView(it.root) }
            .also { binding = it }
            .also {
                setupToolbar()
                setupUi(ThimbleConfiguration())
                setupPermission()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        PermissionRequest(requestCode)
            ?.let { permission ->
                when (permission) {
                    PermissionRequest.OVERLAY -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (Settings.canDrawOverlays(this).not()) {
                                // TODO: Make this a Dialog with explanation and rationale
                                Snackbar.make(binding.root, "Overlay permission denied", Snackbar.LENGTH_SHORT).show()
                            } else {
                                Unit
                            }
                        } else {
                            Unit
                        }
                    }
                    else -> throw NotImplementedError()
                }
            }
    }

    override fun setupUi(configuration: ThimbleConfiguration) {
        (binding.toolbar.menu.findItem(R.id.status).actionView as? SwitchMaterial)?.let {
            it.setOnCheckedChangeListener(null)
            it.isChecked = configuration.enabled
            it.setOnCheckedChangeListener { _, isChecked ->
                when (isChecked) {
                    true -> createService()
                    false -> destroyService()
                }
            }
        }
        with(supportFragmentManager) {
            (findFragmentById(R.id.gridOverlayFragment) as? GridOverlayFragment)?.let {
                it.toggleUi(configuration.enabled)
                it.configure(configuration.grid)
            }
            (findFragmentById(R.id.mockupOverlayFragment) as? MockupOverlayFragment)?.let {
                it.toggleUi(configuration.enabled)
                it.configure(configuration.mockup)
            }
            (findFragmentById(R.id.magnifierOverlayFragment) as? MagnifierOverlayFragment)?.let {
                it.toggleUi(configuration.enabled)
                it.configure(configuration.magnifier)
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this).not()) {
                startActivityForResult(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    ),
                    PermissionRequest.OVERLAY.requestCode
                )
            } else {
                Unit
            }
        } else {
            Unit
        }
    }
}