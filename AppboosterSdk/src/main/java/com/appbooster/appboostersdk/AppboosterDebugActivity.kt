package com.appbooster.appboostersdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Created at 11.08.2020 17:57
 * @author Alexey_Ivanov
 */
class AppboosterDebugActivity : AppCompatActivity() {

    private val store: Store by lazy { Store.getInstance(this) }
    private val inflater: LayoutInflater by lazy { LayoutInflater.from(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.appbooster_layout_activity_debug)

        val container = findViewById<ViewGroup>(R.id.appboosterExperimentsContainer)
        store.experiments
            .map {
                mapExperimentOnViews(it)
            }
            .forEach {
                container.addView(it)
            }
        findViewById<ImageButton>(R.id.appboosterSaveButton).setOnClickListener {
            store.experimentsDebugDefaults = mExperimentsDebugDefaults
                .map { (k,v) ->
                    Log.d("Appbooster", "Map $k, $v on debug ExperimentDefault")
                    ExperimentDefault(k, v)
                }
        }
    }

    private fun mapExperimentOnViews(experiment: CompositeExperiment): View {

        val experimentContainer = inflater.inflate(R.layout.appbooster_experiment_item, findViewById(R.id.appboosterDebugContainer), false)
        experimentContainer.findViewById<TextView>(R.id.appboosterExperimentTitle).apply {
            text = experiment.name
        }

        experimentContainer.findViewById<TextView>(R.id.appboosterExperimentKey).apply {
            text = experiment.key
        }
        val defaultOption = store.experimentsDebugDefaults.firstOrNull { it.key == experiment.key }?.value?: store.experimentsDefaults.firstOrNull { it.key == experiment.key }?.value ?: ""
        experimentContainer.findViewById<ViewGroup>(R.id.appboosterExperimentOptionsTextLayout).apply {
            experiment.options
                .map {
                    mapExperimentOptionOnViews(it, defaultOption, this)
                }
                .forEach { addView(it) }
        }
        experimentContainer.findViewById<RadioGroup>(R.id.appboosterExperimentOptionsRadioGroup).apply {
            val defaultOption = experiment.options
                .map {
                    mapExperimentOptionOnRadios(experiment, it, defaultOption, this)
                }
                .map {
                    addView(it)
                    ExperimentOptionDefault(it.id, (it.tag as ExperimentOptionTag).default)
                }
                .first {
                    it.default
                }
            check(defaultOption.id)

            setOnCheckedChangeListener { group, checkedId ->
                val checkedOption = group.findViewById<View>(checkedId).tag as ExperimentOptionTag
                Log.d("Appbooster", "Checked option: $checkedOption")
                mExperimentsDebugDefaults[checkedOption.key] = checkedOption.option
            }
        }
        return experimentContainer
    }

    private fun mapExperimentOptionOnViews(option: ExperimentOption, defaultValue: String, container: ViewGroup): View {

        if (option.value == defaultValue) {
            return inflater.inflate(R.layout.appbooster_experiment_option_item_received, container, false)
                .apply {
                    findViewById<TextView>(R.id.appboosterOptionDescription).text = option.description
                    findViewById<TextView>(R.id.appboosterOptionValue).text = option.value
                }
        }
        return inflater.inflate(R.layout.appbooster_experiment_option_item_default, container, false)
            .apply {
                findViewById<TextView>(R.id.appboosterOptionDescription).text = option.description
                findViewById<TextView>(R.id.appboosterOptionValue).text = option.value
            }
    }

    private fun mapExperimentOptionOnRadios(
        experiment: CompositeExperiment,
        option: ExperimentOption,
        defaultValue: String,
        container: ViewGroup
    ): View {
        val tag = ExperimentOptionTag(experiment.key, option.value, option.value == defaultValue)
        if (option.value == defaultValue) {
            return inflater.inflate(R.layout.appbooster_experiment_option_radio_received, container, false).apply {
                this.tag = tag
            }
        }
        return inflater.inflate(R.layout.appbooster_experiment_option_radio_default, container, false).apply {
            this.tag = tag
        }
    }

    data class ExperimentOptionTag(
        val key: String,
        val option: String,
        val default: Boolean
    )

    data class ExperimentOptionDefault(
        val id: Int,
        val default: Boolean
    )


    private val mExperimentsDebugDefaults = HashMap<String, String>()

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, AppboosterDebugActivity::class.java)
                .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(intent)
        }
    }
}
