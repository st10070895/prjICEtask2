package com.example.firebasebasics

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    val TAG = "Firebase"
    lateinit var listView: ListView
    lateinit var addButton: Button
    lateinit var editText1: EditText
    lateinit var editText2: EditText
    lateinit var adapter: ArrayAdapter<String>
    lateinit var myRefName: DatabaseReference
    lateinit var myRefFood: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.listView)
        addButton = findViewById(R.id.addButton)
        editText1 = findViewById(R.id.txtName)
        editText2 = findViewById(R.id.txtFavFood)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        listView.adapter = adapter

        myRefName = FirebaseDatabase.getInstance().getReference("Name")
        myRefFood = FirebaseDatabase.getInstance().getReference("FavouriteFood")

        addButton.setOnClickListener {
            val inputNameText = editText1.text.toString()
            val inputFoodText = editText2.text.toString()

            if (inputNameText.isNotEmpty() && inputFoodText.isNotEmpty()) {
                val item = "$inputNameText - $inputFoodText"
                // Add item to the adapter and Firebase separately
                adapter.add(item)
                myRefName.push().setValue(inputNameText)
                myRefFood.push().setValue(inputFoodText)
                editText1.text.clear()
                editText2.text.clear()
            }
        }

        // Read existing items from Firebase for both Name and FavouriteFood
        myRefName.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                adapter.clear()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(String::class.java)
                    item?.let { adapter.add(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        myRefFood.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // No need to do anything here, as we're not displaying this data directly
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // Set up item click listener to delete items
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapter.getItem(position)
            selectedItem?.let { deleteItemFromFirebase(it) }
            adapter.remove(selectedItem)
        }
    }

    private fun deleteItemFromFirebase(item: String) {
        myRefName.orderByValue().equalTo(item).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    childSnapshot.ref.removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        myRefFood.orderByValue().equalTo(item).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    childSnapshot.ref.removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}

