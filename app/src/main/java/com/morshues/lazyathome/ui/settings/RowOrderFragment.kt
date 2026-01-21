package com.morshues.lazyathome.ui.settings

import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.morshues.lazyathome.R
import com.morshues.lazyathome.settings.RowSetting
import com.morshues.lazyathome.settings.SettingsManager
import com.morshues.lazyathome.ui.bangga.BanggaRowController
import com.morshues.lazyathome.ui.library.LibraryRowController
import com.morshues.lazyathome.ui.tg.TgVideoRowController
import com.morshues.lazyathome.ui.linkpage.LinkPageRowController
import dagger.hilt.android.AndroidEntryPoint
import java.util.Collections
import javax.inject.Inject

@AndroidEntryPoint
class RowOrderFragment : Fragment() {

    @Inject
    lateinit var settingsManager: SettingsManager

    private lateinit var rows: MutableList<RowSetting>
    private lateinit var adapter: ArrayAdapter<RowSetting>
    private lateinit var listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            isFocusable = true
            isFocusableInTouchMode = true
        }

        val instructions = TextView(context).apply {
            text = context.getString(R.string.settings_row_order_instructions)
            setTextColor(Color.WHITE)
            textSize = 18f
            setPadding(24, 24, 24, 24)
        }
        layout.addView(instructions)

        // ListView
        listView = ListView(context)
        layout.addView(listView)

        rows = settingsManager.getRowOrderWithEnabled().ifEmpty {
            DEFAULT_ROW_OPTIONS.toMutableList()
        }

        adapter = object : ArrayAdapter<RowSetting>(
            context,
            android.R.layout.simple_list_item_activated_1,
            rows
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                val row = rows[position]
                val rowName = when (row.id) {
                    LibraryRowController.ID -> getString(R.string.row_library)
                    TgVideoRowController.ID -> getString(R.string.row_tg_video)
                    BanggaRowController.ID -> getString(R.string.row_bangga)
                    LinkPageRowController.ID -> getString(R.string.row_link_page)
                    else -> row.id
                }
                view.setBackgroundResource(R.drawable.row_item_background)
                view.text = if (row.enabled) "✅ $rowName" else "❌ $rowName"
                view.isEnabled = row.enabled

                return view
            }
        }
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE
        listView.setItemChecked(0, true)

        return layout
    }

    override fun onResume() {
        super.onResume()
        requireView().requestFocus()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setOnKeyListener { _, keyCode, event ->
            if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false
            val pos = listView.checkedItemPosition
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    if (pos > 0) {
                        listView.setItemChecked(pos - 1, true)
                    }
                    return@setOnKeyListener true
                }

                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    if (pos < rows.size - 1) {
                        listView.setItemChecked(pos + 1, true)
                    }
                    return@setOnKeyListener true
                }

                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    if (pos > 0) {
                        Collections.swap(rows, pos, pos - 1)
                        adapter.notifyDataSetChanged()
                        listView.setItemChecked(pos - 1, true)
                    }
                    return@setOnKeyListener true
                }

                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (pos < rows.size - 1) {
                        Collections.swap(rows, pos, pos + 1)
                        adapter.notifyDataSetChanged()
                        listView.setItemChecked(pos + 1, true)
                    }
                    return@setOnKeyListener true
                }

                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                    rows.getOrNull(pos)?.let {
                        it.enabled = !it.enabled
                        adapter.notifyDataSetChanged()
                    }
                    return@setOnKeyListener true
                }

                KeyEvent.KEYCODE_BACK -> {
                    saveSettings()
                    parentFragmentManager.popBackStack()
                    return@setOnKeyListener true
                }
            }
            false
        }
    }

    private fun saveSettings() {
        settingsManager.saveRowOrderAndEnabled(rows)
    }

    companion object {
        val DEFAULT_ROW_OPTIONS = listOf(
            RowSetting(BanggaRowController.ID, true),
            RowSetting(LibraryRowController.ID, false),
            RowSetting(TgVideoRowController.ID, false),
            RowSetting(LinkPageRowController.ID, false),
        )
    }
}