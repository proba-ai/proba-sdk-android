package ai.proba.probasdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Created at 11.08.2020 17:57
 * @author Alexey_Ivanov
 */
class ProbaDebugActivity : AppCompatActivity() {

    private val store: Store by lazy { Store.getInstance(this) }
    private val inflater: LayoutInflater by lazy { LayoutInflater.from(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.proba_layout_activity_debug)

        val container = findViewById<ViewGroup>(R.id.probaExperimentsContainer)

        findViewById<ImageButton>(R.id.probaSaveButton).setOnClickListener {
            store.experimentsDebug = mExperimentsDebugDefaults
                .map { (k,v) ->
                    Experiment(k, v)
                }
            finish()
        }
        findViewById<Button>(R.id.probaResetButton).setOnClickListener {
            mExperimentsDebugDefaults.clear()
            store.experimentsDebug = emptyList()
            container.removeAllViews()
            store.experiments
                .map {
                    mapExperimentOnViews(it)
                }
                .forEach {
                    container.addView(it)
                }
        }

        store.experiments
            .map {
                mapExperimentOnViews(it)
            }
            .forEach {
                container.addView(it)
            }
    }

    private fun mapExperimentOnViews(experiment: CompositeExperiment): View {
        val experimentContainer = inflater.inflate(R.layout.proba_experiment_item, findViewById(R.id.probaDebugContainer), false)
        experimentContainer.findViewById<TextView>(R.id.probaExperimentTitle).apply {
            text = experiment.name
        }

        experimentContainer.findViewById<TextView>(R.id.probaExperimentKey).apply {
            text = experiment.key
        }
        val receivedOption = store.experimentsDefaults.firstOrNull { it.key == experiment.key }?.value ?: ""
        val defaultOption = store.experimentsDebug.firstOrNull { it.key == experiment.key }?.value?: receivedOption
        experimentContainer.findViewById<ViewGroup>(R.id.probaExperimentOptionsTextLayout).apply {
            experiment.options
                .map {
                    mapExperimentOptionOnViews(it, receivedOption, this)
                }
                .forEach { addView(it) }
        }
        experimentContainer.findViewById<RadioGroup>(R.id.probaExperimentOptionsRadioGroup).apply {
            val defaultOption = experiment.options
                .map {
                    mapExperimentOptionOnRadios(experiment, it, receivedOption, defaultOption, this)
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
                mExperimentsDebugDefaults[checkedOption.key] = checkedOption.option
            }
        }
        return experimentContainer
    }

    private fun mapExperimentOptionOnViews(option: ExperimentOption, receivedOption: String, container: ViewGroup): View {

        if (option.value == receivedOption) {
            return inflater.inflate(R.layout.proba_experiment_option_item_received, container, false)
                .apply {
                    findViewById<TextView>(R.id.probaOptionDescription).text = option.description
                    findViewById<TextView>(R.id.probaOptionValue).text = option.value
                }
        }
        return inflater.inflate(R.layout.proba_experiment_option_item_default, container, false)
            .apply {
                findViewById<TextView>(R.id.probaOptionDescription).text = option.description
                findViewById<TextView>(R.id.probaOptionValue).text = option.value
            }
    }

    private fun mapExperimentOptionOnRadios(
        experiment: CompositeExperiment,
        option: ExperimentOption,
        receivedOption: String,
        defaultOption: String,
        container: ViewGroup
    ): View {
        val tag = ExperimentOptionTag(experiment.key, option.value, option.value == defaultOption)
        if (option.value == receivedOption) {
            return inflater.inflate(R.layout.proba_experiment_option_radio_received, container, false).apply {
                this.tag = tag
            }
        }
        return inflater.inflate(R.layout.proba_experiment_option_radio_default, container, false).apply {
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
        @JvmStatic
        internal fun launch(context: Context) {
            val intent = Intent(context, ProbaDebugActivity::class.java)
                .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(intent)
        }
    }
}
