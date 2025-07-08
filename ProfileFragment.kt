package com.example.fitiq

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var editProfileButton: ImageButton
    private lateinit var profileName: TextView

    private lateinit var reminderRow: View
    private lateinit var rateUsRow: View
    private lateinit var termsRow: View
    private lateinit var changeGoalRow: View
    private lateinit var changeWeightHeightRow: View
    private lateinit var logoutRow: View

    private lateinit var facebookButton: ImageButton
    private lateinit var instagramButton: ImageButton
    private lateinit var twitterButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileImage = view.findViewById(R.id.profileImage)
        editProfileButton = view.findViewById(R.id.editProfile)
        profileName = view.findViewById(R.id.profileName)

        reminderRow = view.findViewById(R.id.reminderRow)
        rateUsRow = view.findViewById(R.id.rateUsRow)
        termsRow = view.findViewById(R.id.termsRow)
        changeGoalRow = view.findViewById(R.id.changeGoalRow)
        changeWeightHeightRow = view.findViewById(R.id.changeWeightHeightRow)
        logoutRow = view.findViewById(R.id.logoutRow)

        facebookButton = view.findViewById(R.id.facebookButton)
        instagramButton = view.findViewById(R.id.instagramButton)
        twitterButton = view.findViewById(R.id.twitterButton)

        // Display Google Account name
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (account != null) {
            profileName.text = account.displayName ?: "User"
        } else {
            profileName.text = "User"
        }

        setupListeners()
    }

    private fun setupListeners() {
        editProfileButton.setOnClickListener {
            Toast.makeText(requireContext(), "Edit profile clicked", Toast.LENGTH_SHORT).show()
        }

        reminderRow.setOnClickListener {
            Toast.makeText(requireContext(), "Reminder clicked", Toast.LENGTH_SHORT).show()
        }

        rateUsRow.setOnClickListener {
            Toast.makeText(requireContext(), "Rate us clicked", Toast.LENGTH_SHORT).show()
        }

        termsRow.setOnClickListener {
            Toast.makeText(requireContext(), "Terms & Conditions clicked", Toast.LENGTH_SHORT).show()
        }

        changeGoalRow.setOnClickListener {
            Toast.makeText(requireContext(), "Change Step Goal clicked", Toast.LENGTH_SHORT).show()
        }

        changeWeightHeightRow.setOnClickListener {
            Toast.makeText(requireContext(), "Change Weight/Height clicked", Toast.LENGTH_SHORT).show()
        }

        logoutRow.setOnClickListener {
            logout()
        }

        facebookButton.setOnClickListener {
            openUrl("https://www.facebook.com/")
        }

        instagramButton.setOnClickListener {
            openUrl("https://www.instagram.com/")
        }

        twitterButton.setOnClickListener {
            openUrl("https://twitter.com/")
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        googleSignInClient.signOut().addOnCompleteListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
