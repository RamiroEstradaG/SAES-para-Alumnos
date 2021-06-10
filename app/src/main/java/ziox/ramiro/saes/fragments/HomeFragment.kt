package ziox.ramiro.saes.fragments

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.models.Tweet
import com.twitter.sdk.android.tweetui.TimelineResult
import com.twitter.sdk.android.tweetui.TweetView
import com.twitter.sdk.android.tweetui.UserTimeline
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.databases.*
import ziox.ramiro.saes.databinding.FragmentHomeBinding
import ziox.ramiro.saes.databinding.ViewGradesSmallCardBinding
import ziox.ramiro.saes.databinding.ViewRecentActivityBinding
import ziox.ramiro.saes.databinding.ViewTweetCardBinding
import ziox.ramiro.saes.utils.addBottomInsetPadding
import ziox.ramiro.saes.utils.addTopInsetPadding
import ziox.ramiro.saes.utils.getInitials
import ziox.ramiro.saes.utils.isDarkTheme
import kotlin.math.max

class HomeFragment : Fragment() {
    private val tweets = ArrayList<Tweet>()
    private val grades = ArrayList<CourseGrade>()
    private lateinit var rootView: FragmentHomeBinding
    private lateinit var recentActivityDao: RecentActivityDao
    private lateinit var gradesDao: GradesDao
    private lateinit var kardexDao: KardexDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = FragmentHomeBinding.inflate(inflater, container, false)
        recentActivityDao = AppLocalDatabase.getInstance(requireContext()).recentActivityDao()
        gradesDao = AppLocalDatabase.getInstance(requireContext()).gradesDao()
        kardexDao = AppLocalDatabase.getInstance(requireContext()).kardexDao()
        rootView.tweetsRecycler.adapter = TwitterAdapter()
        rootView.tweetsRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rootView.gradesRecycler.adapter = GradesAdapter()
        rootView.gradesRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rootView.root.addTopInsetPadding()
        rootView.root.addBottomInsetPadding()
        rootView.tweetsRecycler.recycledViewPool.setMaxRecycledViews(0,0)

        fetchKardexData()
        fetchGradesData()
        fetchRecentActivity()
        fetchTweets()

        return rootView.root
    }

    private fun fetchKardexData(){
        val kardexData = kardexDao.getAll().filter { it.semester != "_" }

        if(kardexData.isEmpty()){
            rootView.kardexSection.visibility = View.GONE
            return
        }

        initChart()

        val scores = ArrayList<Entry>()
        val overallScores = ArrayList<Entry>()

        scores.addAll(kardexData.groupBy { it.semester }.values.mapIndexed { index, entry ->
            Entry(
                index.toFloat(),
                entry.sumBy { it.finalScore.toIntOrNull() ?: 0 }.div(entry.size.toDouble()).toFloat()
            )
        })

        val entries = ArrayList<KardexClass>()

        overallScores.addAll(kardexData.groupBy { it.semester }.values.mapIndexed { index, entry ->
            entries.addAll(entry)
            Entry(
                index.toFloat(),
                entries.sumBy { it.finalScore.toIntOrNull() ?: 0 }.div(entries.size.toDouble()).toFloat()
            )
        })

        val scoresDataSet = getScoresDataSet(scores)
        val overallScoresDataSet = getOverallScoresDataSet(overallScores)

        val dataSets = listOf<ILineDataSet>(scoresDataSet, overallScoresDataSet)
        val maxX = max(scores.size, overallScores.size)
        rootView.kardexChart.data = LineData(dataSets)
        rootView.kardexChart.setVisibleXRange(-0.1f, maxX - 0.8f)
        rootView.kardexChart.data.isHighlightEnabled = false
        rootView.kardexChart.invalidate()
        rootView.kardexChart.visibility = View.VISIBLE
    }

    private fun fetchGradesData(){
        grades.clear()
        grades.addAll(gradesDao.getAll())

        if (grades.isEmpty()){
            rootView.gradesSection.visibility = View.GONE
            return
        }

        rootView.gradesRecycler.adapter?.notifyDataSetChanged()
    }

    private fun fetchRecentActivity(){
        val recentActivity = recentActivityDao.getLast(3)

        if (recentActivity.isEmpty()){
            rootView.recentActivitySection.visibility = View.GONE
            return
        }

        if (recentActivity.isNotEmpty()){
            val card = ViewRecentActivityBinding.inflate(layoutInflater, rootView.recentActivityContainer1, true)
            val section = recentActivity[0]
            val icon = BitmapDrawable(resources, BitmapFactory.decodeByteArray(section.sectionIcon, 0, section.sectionIcon.size))

            card.sectionTitleTextView.text = section.sectionName
            card.sectionIcon.setImageDrawable(icon)

            card.root.setOnClickListener {
                val id = resources.getIdentifier(section.sectionId, "id", requireContext().packageName)
                (activity as SAESActivity).postNavigationItemSelected(id, false)
            }
        }

        if (recentActivity.size >= 2){
            val card = ViewRecentActivityBinding.inflate(layoutInflater, rootView.recentActivityContainer2, true)
            val section = recentActivity[1]
            val icon = BitmapDrawable(resources, BitmapFactory.decodeByteArray(section.sectionIcon, 0, section.sectionIcon.size))

            card.sectionTitleTextView.text = section.sectionName
            card.sectionIcon.setImageDrawable(icon)

            card.root.setOnClickListener {
                val id = resources.getIdentifier(section.sectionId, "id", requireContext().packageName)
                (activity as SAESActivity).postNavigationItemSelected(id, false)
            }
        }

        if (recentActivity.size >= 3){
            val card = ViewRecentActivityBinding.inflate(layoutInflater, rootView.recentActivityContainer3, true)
            val section = recentActivity[2]
            val icon = BitmapDrawable(resources, BitmapFactory.decodeByteArray(section.sectionIcon, 0, section.sectionIcon.size))

            card.sectionTitleTextView.text = section.sectionName
            card.sectionIcon.setImageDrawable(icon)

            card.root.setOnClickListener {
                val id = resources.getIdentifier(section.sectionId, "id", requireContext().packageName)
                (activity as SAESActivity).postNavigationItemSelected(id, false)
            }
        }
    }

    private fun fetchTweets(){
        val progressBar = (activity as SAESActivity).getProgressBar()

        progressBar?.visibility = View.VISIBLE

        tweets.clear()
        val timelineSec = UserTimeline.Builder()
            .screenName("SecretariaIPN")
            .includeRetweets(false)
            .includeReplies(false)
            .maxItemsPerRequest(5).build()

        val timelineIPN = UserTimeline.Builder()
            .screenName("IPN_MX")
            .includeRetweets(false)
            .includeReplies(false)
            .maxItemsPerRequest(5).build()

        timelineSec.next(null, object : Callback<TimelineResult<Tweet>>(){
            override fun success(result: Result<TimelineResult<Tweet>>) {
                tweets.addAll(result.data.items)
                tweets.sortByDescending { it.createdAt }
                activity?.runOnUiThread {
                    rootView.tweetsRecycler.adapter?.notifyDataSetChanged()
                    progressBar?.visibility = View.GONE
                }
            }
            override fun failure(exception: TwitterException?) {}
        })

        timelineIPN.next(null, object : Callback<TimelineResult<Tweet>>(){
            override fun success(result: Result<TimelineResult<Tweet>>) {
                tweets.addAll(result.data.items)
                tweets.sortByDescending { it.createdAt }
                activity?.runOnUiThread {
                    rootView.tweetsRecycler.adapter?.notifyDataSetChanged()
                    progressBar?.visibility = View.GONE
                }
            }
            override fun failure(exception: TwitterException?) {}
        })
    }

    private fun initChart(){
        rootView.kardexChart.description.text = ""

        rootView.kardexChart.setDrawBorders(false)
        rootView.kardexChart.setNoDataText("Esperando datos")
        rootView.kardexChart.isDoubleTapToZoomEnabled = false
        rootView.kardexChart.setScaleEnabled(false)
        rootView.kardexChart.xAxis.axisMinimum = -0.1f
        rootView.kardexChart.xAxis.setDrawGridLines(false)
        rootView.kardexChart.xAxis.granularity = 1f

        rootView.kardexChart.xAxis.axisLineWidth = 2f
        rootView.kardexChart.xAxis.axisLineColor = ContextCompat.getColor(
            requireContext(),
            R.color.colorTextPrimary
        )
        rootView.kardexChart.xAxis.textColor = ContextCompat.getColor(
            requireContext(),
            R.color.colorTextPrimary
        )
        rootView.kardexChart.xAxis.valueFormatter = IAxisValueFormatter { value, _ -> "${value.toInt() + 1}º" }
        rootView.kardexChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        rootView.kardexChart.xAxis.textSize = 14f

        rootView.kardexChart.axisLeft.granularity = 0.5f
        rootView.kardexChart.axisLeft.setDrawGridLines(true)
        rootView.kardexChart.axisLeft.setDrawZeroLine(false)
        rootView.kardexChart.axisLeft.disableGridDashedLine()
        rootView.kardexChart.axisLeft.gridLineWidth = 1.5f
        rootView.kardexChart.axisLeft.setDrawAxisLine(false)
        rootView.kardexChart.axisLeft.textColor = ContextCompat.getColor(
            requireContext(),
            R.color.colorTextPrimary
        )
        rootView.kardexChart.axisLeft.setLabelCount(2, false)
        rootView.kardexChart.axisLeft.textSize = 12f

        rootView.kardexChart.axisRight.isEnabled = false

        rootView.kardexChart.isDragXEnabled = true
        rootView.kardexChart.scaleX = 1f
        rootView.kardexChart.scaleY = 1f

        rootView.kardexChart.legend.textSize = 12f
        rootView.kardexChart.legend.textColor = ContextCompat.getColor(
            requireContext(),
            R.color.colorTextPrimary
        )

        rootView.kardexChart.invalidate()
    }

    private fun getScoresDataSet(scores : ArrayList<Entry>) : LineDataSet {
        val scoresDataSet = LineDataSet(scores, "Promedio por semestre")
        scoresDataSet.color =
            ContextCompat.getColor(requireContext(), R.color.colorDanger)
        scoresDataSet.valueTextColor = ContextCompat.getColor(
            requireContext(),
            R.color.colorDanger
        )
        scoresDataSet.valueTextSize = 10f
        scoresDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        scoresDataSet.lineWidth = 4f
        scoresDataSet.setCircleColor(Color.TRANSPARENT)
        scoresDataSet.circleHoleColor = Color.TRANSPARENT

        return scoresDataSet
    }

    private fun getOverallScoresDataSet(overallScores : ArrayList<Entry>) : LineDataSet {
        val promedioDataSet = LineDataSet(overallScores, "Promedio global")
        promedioDataSet.color = ContextCompat.getColor(
            requireContext(),
            R.color.colorInfo
        )
        promedioDataSet.setCircleColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorTextPrimary
            )
        )
        promedioDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        promedioDataSet.enableDashedLine(32f, 12f, 1f)
        promedioDataSet.lineWidth = 4f
        promedioDataSet.valueTextSize = 10f
        promedioDataSet.valueTextColor = ContextCompat.getColor(
            requireContext(),
            R.color.colorTextPrimary
        )
        return promedioDataSet
    }

    inner class TwitterAdapter : RecyclerView.Adapter<TwitterAdapter.ViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TwitterAdapter.ViewHolder {
            return ViewHolder(ViewTweetCardBinding.inflate(LayoutInflater.from(context), parent, false))
        }

        override fun getItemId(position: Int) = tweets[position].getId()

        override fun getItemViewType(position: Int) = 0

        override fun onBindViewHolder(holder: TwitterAdapter.ViewHolder, position: Int) {
            try{
                holder.tweetCard.root.addView(TweetView(context, tweets[position], if(isDarkTheme(context)){
                    com.twitter.sdk.android.tweetui.R.style.tw__TweetDarkStyle
                }else{
                    com.twitter.sdk.android.tweetui.R.style.tw__TweetLightStyle
                }))
            }catch (e: Exception){
                e.printStackTrace()
            }
            holder.setIsRecyclable(false)
        }

        override fun getItemCount() = tweets.size

        inner class ViewHolder(val tweetCard: ViewTweetCardBinding) : RecyclerView.ViewHolder(tweetCard.root){

        }
    }

    inner class GradesAdapter : RecyclerView.Adapter<GradesAdapter.ViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradesAdapter.ViewHolder {
            return ViewHolder(ViewGradesSmallCardBinding.inflate(LayoutInflater.from(context), parent, false))
        }

        override fun onBindViewHolder(holder: GradesAdapter.ViewHolder, position: Int) {
            val grade = grades[position]

            holder.name.text = grade.courseName.getInitials()
            holder.grade.text = grade.finalScore

            if(grade.finalScore.toIntOrNull() != null) {
                if (grade.finalScore.toInt() < 6) {
                    holder.grade.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDanger))
                }else{
                    holder.grade.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorInfo))
                }
            }else{
                holder.grade.text = "—"
            }

            holder.selfButton.setOnClickListener {
                (activity as SAESActivity).postNavigationItemSelected(R.id.nav_calific, false)
            }
        }

        override fun getItemCount() = grades.size

        inner class ViewHolder(smallCardBinding: ViewGradesSmallCardBinding) : RecyclerView.ViewHolder(smallCardBinding.root){
            val name = smallCardBinding.courseName
            val grade = smallCardBinding.gradeTextView
            val selfButton = smallCardBinding.root
        }
    }
}