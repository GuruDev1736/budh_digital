package com.naturexpresscargo.pressapp

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.naturexpresscargo.pressapp.fragments.ForumFragment
import com.naturexpresscargo.pressapp.fragments.ServiceFragment
import com.naturexpresscargo.pressapp.fragments.HistoryFragment
import fragments.ProfileFragment

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)

        setupEdgeToEdge()

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)

        loadUserProfile()

        if (savedInstanceState == null) {
            loadFragment(ServiceFragment())
            supportActionBar?.title = "Our Services"
            navigationView.setCheckedItem(R.id.nav_service)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                        drawerLayout.closeDrawer(GravityCompat.START)
                    }
                    navigationView.checkedItem?.itemId != R.id.nav_service -> {
                        // Navigate back to Service tab if not already there
                        loadFragment(ServiceFragment())
                        supportActionBar?.title = "Our Services"
                        navigationView.setCheckedItem(R.id.nav_service)
                    }
                    else -> {
                        // Already on Service tab, exit the app
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            toolbar.setPadding(
                0,
                insets.top,
                0,
                0
            )

            findViewById<FrameLayout>(R.id.fragment_container).setPadding(
                0,
                0,
                0,
                insets.bottom
            )

            windowInsets
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                loadFragment(ProfileFragment())
                supportActionBar?.title = "Profile"
            }
            R.id.nav_service -> {
                loadFragment(ServiceFragment())
                supportActionBar?.title = "Our Services"
            }
            R.id.nav_history -> {
                loadFragment(HistoryFragment())
                supportActionBar?.title = "Request History"
            }
            R.id.nav_forum -> {
                loadFragment(ForumFragment())
                supportActionBar?.title = "Forums"
            }
            R.id.nav_logout -> {
                showLogoutDialog()
                return true
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                performLogout()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            .setCancelable(true)
            .show()
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val headerView = navigationView.getHeaderView(0)
            val tvProfileName = headerView.findViewById<TextView>(R.id.profile_name)
            val tvProfileLevel = headerView.findViewById<TextView>(R.id.profile_level)

            val userId = currentUser.uid
            val userRef = database.reference.child("users").child(userId)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val fullName = snapshot.child("fullName").getValue(String::class.java) ?: "User"
                        val email = snapshot.child("email").getValue(String::class.java) ?: currentUser.email ?: ""

                        tvProfileName.text = fullName
                        tvProfileLevel.text = email
                    } else {
                        tvProfileName.text = currentUser.email?.substringBefore("@") ?: "User"
                        tvProfileLevel.text = currentUser.email ?: ""
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    tvProfileName.text = currentUser.email?.substringBefore("@") ?: "User"
                    tvProfileLevel.text = currentUser.email ?: ""
                }
            })
        }
    }

    private fun performLogout() {
        auth.signOut()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
