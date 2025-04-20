package com.morshues.lazyathome

import java.util.Timer
import java.util.TimerTask

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.OnItemViewSelectedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.core.content.ContextCompat
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider

import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.morshues.lazyathome.data.network.RetrofitClient
import com.morshues.lazyathome.settings.SettingsManager
import com.morshues.lazyathome.ui.bangga.BanggaRowController
import com.morshues.lazyathome.ui.bangga.BanggaViewModel
import com.morshues.lazyathome.ui.bangga.BanggaViewModelFactory
import com.morshues.lazyathome.ui.common.BaseRowController
import com.morshues.lazyathome.ui.common.RowInfo
import com.morshues.lazyathome.ui.library.LibraryRowController
import com.morshues.lazyathome.ui.library.LibraryViewModel
import com.morshues.lazyathome.ui.library.LibraryViewModelFactory
import com.morshues.lazyathome.ui.settings.SettingsActivity
import com.morshues.lazyathome.ui.settings.SettingsRowController
import com.morshues.lazyathome.ui.tg.TgVideoRowController
import com.morshues.lazyathome.ui.tg.TgVideoViewModel
import com.morshues.lazyathome.ui.tg.TgVideoViewModelFactory

/**
 * Loads a grid of cards with movies to browse.
 */
class MainFragment : BrowseSupportFragment() {

    private val mHandler = Handler(Looper.myLooper()!!)
    private lateinit var mBackgroundManager: BackgroundManager
    private var mDefaultBackground: Drawable? = null
    private lateinit var mMetrics: DisplayMetrics
    private var mBackgroundTimer: Timer? = null
    private var mBackgroundUri: String? = null

    private lateinit var libraryViewModel: LibraryViewModel
    private lateinit var tgVideosViewModel: TgVideoViewModel
    private lateinit var banggaViewModel: BanggaViewModel
    private lateinit var allRowInfos: List<RowInfo>

    private val rowToControllerMap = mutableMapOf<Row, BaseRowController>()
    private var currentSelectedRow: Row? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (isShowingHeaders || !handleBackInRows()) {
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        }
    }

    private fun handleBackInRows(): Boolean {
        return currentSelectedRow
            ?.let { rowToControllerMap[it] }
            ?.handleBackPress() ?: false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        prepareBackgroundManager()
        setupUIElements()
        resetViewModels()
        initRows()
        loadRows()
        setupEventListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: " + mBackgroundTimer?.toString())
        mBackgroundTimer?.cancel()
    }

    private fun prepareBackgroundManager() {

        mBackgroundManager = BackgroundManager.getInstance(activity)
        mBackgroundManager.attach(requireActivity().window)
        mDefaultBackground = ContextCompat.getDrawable(requireActivity(), R.drawable.default_background)
        mMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(mMetrics)
    }

    private fun setupUIElements() {
        title = getString(R.string.browse_title)
        // over title
        headersState = BrowseSupportFragment.HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        // set fastLane (or headers) background color
        brandColor = ContextCompat.getColor(requireActivity(), R.color.fastlane_background)
        // set search icon color
        searchAffordanceColor = ContextCompat.getColor(requireActivity(), R.color.search_opaque)
    }

    private fun resetViewModels() {
        viewModelStore.clear()
        val service = RetrofitClient.getService(requireContext())
        tgVideosViewModel = ViewModelProvider(this, TgVideoViewModelFactory(
            service
        ))[TgVideoViewModel::class.java]
        libraryViewModel = ViewModelProvider(this, LibraryViewModelFactory(
            service
        ))[LibraryViewModel::class.java]
        banggaViewModel = ViewModelProvider(this, BanggaViewModelFactory(
        ))[BanggaViewModel::class.java]
    }

    private fun initRows() {
        allRowInfos = listOf(
            RowInfo(LibraryRowController.ID) {
                LibraryRowController(
                    getString(R.string.row_library),
                    requireActivity(),
                    libraryViewModel,
                )
            },
            RowInfo(TgVideoRowController.ID) {
                TgVideoRowController(
                    getString(R.string.row_tg_video),
                    requireActivity(),
                    tgVideosViewModel
                )
            },
            RowInfo(BanggaRowController.ID) {
                BanggaRowController(
                    getString(R.string.row_bangga),
                    requireActivity(),
                    banggaViewModel,
                )
            },
        )
    }

    private fun loadRows() {
        val settings = SettingsManager.getRowOrderWithEnabled(requireContext())
        val enabledRowIds = settings.filter { it.enabled }.map { it.id }

        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        rowToControllerMap.clear()
        enabledRowIds.forEach { id ->
            val info = allRowInfos.find { it.id == id }
            if (info != null) {
                val controller = info.controllerProvider()
                rowToControllerMap[controller.listRow] = controller
                rowsAdapter.add(controller.listRow)
                controller.loadData()
            }
        }

        val settingsRowController = SettingsRowController(
            getString(R.string.others),
            requireActivity()
        ) {
            settingsLauncher.launch(Intent(requireContext(), SettingsActivity::class.java))
        }
        rowToControllerMap[settingsRowController.listRow] = settingsRowController
        rowsAdapter.add(settingsRowController.listRow)

        adapter = rowsAdapter
    }

    private fun setupEventListeners() {
        setOnSearchClickedListener {
            Toast.makeText(requireActivity(), "Implement your own in-app search", Toast.LENGTH_LONG)
                .show()
        }

        onItemViewClickedListener = ItemViewClickedListener()
        onItemViewSelectedListener = ItemViewSelectedListener()
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder,
            item: Any,
            rowViewHolder: RowPresenter.ViewHolder,
            row: Row
        ) {
            rowToControllerMap[row]?.onClick(item)
                ?: run {
                    if (item is String) {
                        Log.d(TAG, "Item: $item")
                        Toast.makeText(requireContext(), item, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(
            itemViewHolder: Presenter.ViewHolder?, item: Any?,
            rowViewHolder: RowPresenter.ViewHolder, row: Row
        ) {
            currentSelectedRow = row
            rowToControllerMap[row]?.getBackgroundUri(item)?.let {
                mBackgroundUri = it
                startBackgroundTimer()
            }
        }
    }

    private val settingsLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            RetrofitClient.reset()
            resetViewModels()
            initRows()
            loadRows()
        }
    }

    private fun updateBackground(uri: String?) {
        val width = mMetrics.widthPixels
        val height = mMetrics.heightPixels
        Glide.with(requireActivity())
            .load(uri)
            .centerCrop()
            .error(mDefaultBackground)
            .into(object : CustomTarget<Drawable>(width, height) {
                    override fun onResourceReady(
                        drawable: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        mBackgroundManager.drawable = drawable
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        mBackgroundTimer?.cancel()
    }

    private fun startBackgroundTimer() {
        mBackgroundTimer?.cancel()
        mBackgroundTimer = Timer()
        mBackgroundTimer?.schedule(UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY.toLong())
    }

    private inner class UpdateBackgroundTask : TimerTask() {

        override fun run() {
            mHandler.post { updateBackground(mBackgroundUri) }
        }
    }

    companion object {
        private const val TAG = "MainFragment"

        private const val BACKGROUND_UPDATE_DELAY = 300
    }
}