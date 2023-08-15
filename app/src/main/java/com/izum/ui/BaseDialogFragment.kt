package com.izum.ui

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.izum.di.IoDispatcher
import com.izum.ui.route.Router
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import javax.inject.Inject

abstract class BaseDialogFragment : DialogFragment(), CoroutineScope by MainScope() {

    protected val onBackPressedDispatcher by lazy { requireActivity().onBackPressedDispatcher }

    private val router: Router
        get() = (requireActivity() as BaseActivity).router

    @IoDispatcher
    @Inject
    lateinit var ioDispatcher: CoroutineDispatcher

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            arguments?.let(::initArgs)
        } catch (ex: Exception) {
            Log.e("Steve", "BaseDialogFragment: onCreate: ${ex.message}")
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initSubs()
    }

    @Throws(Exception::class)
    open fun initArgs(args: Bundle) {}
    open fun initView() {}
    open fun initSubs() {}
}