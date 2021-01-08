package ziox.ramiro.saes.fragments

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        fetchKardexData()
        fetchGradesData()
        fetchRecentActivity()
        fetchTweets()

        return rootView.root
    }

    private fun fetchKardexData(){
        val kardexData = kardexDao.getAll()

        rootView.kardexSection.visibility = View.GONE

        if(kardexData.isEmpty()){
            rootView.kardexSection.visibility = View.GONE
            return
        }
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
        tweets.clear()
        val timeline = UserTimeline.Builder()
            .screenName("SecretariaIPN")
            .includeRetweets(false)
            .includeReplies(false)
            .maxItemsPerRequest(10).build()
        timeline.next(null, object : Callback<TimelineResult<Tweet>>(){
            override fun success(result: Result<TimelineResult<Tweet>>) {
                tweets.addAll(result.data.items)
                activity?.runOnUiThread {
                    rootView.tweetsRecycler.adapter?.notifyDataSetChanged()
                }
            }
            override fun failure(exception: TwitterException?) {}
        })
    }

    inner class TwitterAdapter : RecyclerView.Adapter<TwitterAdapter.ViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TwitterAdapter.ViewHolder {
            return ViewHolder(ViewTweetCardBinding.inflate(LayoutInflater.from(context), parent, false))
        }

        override fun onBindViewHolder(holder: TwitterAdapter.ViewHolder, position: Int) {
            holder.tweetCard.root.addView(TweetView(context, tweets[position]))
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