package com.example.petDiary.ui.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import com.example.petDiary.R
import com.example.petDiary.data.models.EventDto
import com.example.petDiary.data.models.PetProfileDto
import com.example.petDiary.ui.viewmodel.HomeViewModel
import com.google.android.material.card.MaterialCardView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class HomeFragment : Fragment() {
    private lateinit var viewModel: HomeViewModel
    private lateinit var tvTipEmoji: TextView
    private lateinit var tvTipTitle: TextView
    private lateinit var tvTipText: TextView

    // Элементы для событий на сегодня
    private lateinit var cardTodayEvents: MaterialCardView
    private lateinit var llTodayEventsList: LinearLayout
    private lateinit var tvNoTodayEvents: TextView

    // Элементы карточки питомца
    private lateinit var tvPetName: TextView
    private lateinit var tvPetBreed: TextView
    private lateinit var tvPetAge: TextView
    private lateinit var ivPetPhoto: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        initViews(view)
        setupObservers()
        viewModel.refreshData()
        loadRandomTip()
    }

    private fun initViews(view: View) {
        tvTipEmoji = view.findViewById(R.id.tvTipEmoji)
        tvTipTitle = view.findViewById(R.id.tvTipTitle)
        tvTipText = view.findViewById(R.id.tvTipText)

        tvPetName = view.findViewById(R.id.tvPetName)
        tvPetBreed = view.findViewById(R.id.tvPetBreed)
        tvPetAge = view.findViewById(R.id.tvPetAge)
        ivPetPhoto = view.findViewById(R.id.ivPetPhoto)

        cardTodayEvents = view.findViewById(R.id.cardTodayEvents)
        llTodayEventsList = view.findViewById(R.id.llTodayEventsList)
        tvNoTodayEvents = view.findViewById(R.id.tvNoTodayEvents)
    }

    private fun setupObservers() {
        viewModel.petProfile.observe(viewLifecycleOwner, Observer { profile: PetProfileDto? ->
            profile?.let { updatePetProfile(it) }
        })

        viewModel.todayEvents.observe(viewLifecycleOwner, Observer { events: List<EventDto>? ->
            events?.let { updateTodayEvents(it) }
        })
    }

    private fun updatePetProfile(profile: PetProfileDto) {
        tvPetName.text = if (profile.name.isNotBlank()) profile.name else "Имя не указано"
        tvPetBreed.text = if (profile.breed.isNotBlank()) profile.breed else "Порода не указана"

        if (profile.birthDate.isNotBlank()) {
            try {
                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                val birthDate = sdf.parse(profile.birthDate)
                if (birthDate != null) {
                    val now = Calendar.getInstance()
                    val birth = Calendar.getInstance().apply { time = birthDate }
                    var age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
                    if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) age--

                    val ageText = when {
                        age % 10 == 1 && age % 100 != 11 -> "$age год"
                        age % 10 in 2..4 && age % 100 !in 12..14 -> "$age года"
                        else -> "$age лет"
                    }
                    tvPetAge.text = ageText
                    tvPetAge.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                tvPetAge.visibility = View.GONE
            }
        } else {
            tvPetAge.visibility = View.GONE
        }

        loadPetPhoto(profile.photoPath)
    }

    private fun loadPetPhoto(photoPath: String?) {
        ivPetPhoto.setPadding(0, 0, 0, 0)

        if (photoPath != null) {
            try {
                val file = File(photoPath)
                if (file.exists() && file.length() > 0) {
                    val bitmap = BitmapFactory.decodeFile(photoPath)
                    if (bitmap != null) {
                        ivPetPhoto.setImageBitmap(bitmap)
                        return
                    }
                }

                if (photoPath.startsWith("https://")) {
                    // TODO: Загрузить по другому
                    // Glide.with(this).load(photoPath).into(ivPetPhoto)
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        ivPetPhoto.setImageResource(R.drawable.ic_paw1)
        ivPetPhoto.setPadding(12, 12, 12, 12)
    }

    private fun updateTodayEvents(events: List<EventDto>) {
        llTodayEventsList.removeAllViews()
        if (events.isEmpty()) {
            tvNoTodayEvents.visibility = View.VISIBLE
            llTodayEventsList.visibility = View.GONE
        } else {
            tvNoTodayEvents.visibility = View.GONE
            llTodayEventsList.visibility = View.VISIBLE
            events.take(5).forEach { event ->
                val eventView = createTodayEventView(event)
                llTodayEventsList.addView(eventView)
            }

            if (events.size > 5) {
                val moreView = TextView(requireContext()).apply {
                    text = "... и ещё ${events.size - 5}"
                    setTextColor(resources.getColor(R.color.grey, null))
                    textSize = 12f
                    setPadding(0, 8, 0, 0)
                }
                llTodayEventsList.addView(moreView)
            }
        }
    }

    private fun createTodayEventView(event: EventDto): View {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_today_event_home, llTodayEventsList, false)
        val tvEventTime = view.findViewById<TextView>(R.id.tvEventTime)
        val tvEventTitle = view.findViewById<TextView>(R.id.tvEventTitle)

        val time = event.time ?: ""
        if (time.isNotEmpty()) {
            tvEventTime.text = time
            tvEventTime.visibility = View.VISIBLE
        } else {
            tvEventTime.visibility = View.GONE
        }

        tvEventTitle.text = event.title
        return view
    }

    private fun loadRandomTip() {
        val emojis = resources.getStringArray(R.array.tip_emojis)
        val titles = resources.getStringArray(R.array.tip_titles)
        val texts = resources.getStringArray(R.array.tip_texts)

        if (emojis.isNotEmpty() && titles.isNotEmpty() && texts.isNotEmpty()) {
            val randomIndex = Random.nextInt(emojis.size)
            tvTipEmoji.text = emojis[randomIndex]
            tvTipTitle.text = titles[randomIndex]
            tvTipText.text = texts[randomIndex]
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}