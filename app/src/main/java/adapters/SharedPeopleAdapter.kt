package adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hw1.R

class SharedPeopleAdapter(private val sharedPeople: List<String>) :
    RecyclerView.Adapter<SharedPeopleAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userEmailTextView: TextView = view.findViewById(R.id.user_email)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.shared_people_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.userEmailTextView.text = sharedPeople[position]
    }

    override fun getItemCount() = sharedPeople.size
}
