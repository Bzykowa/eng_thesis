package com.example.lockband

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.lockband.databinding.FragmentChangePasswordBinding
import com.example.lockband.utils.PASS_FILE
import com.example.lockband.utils.hashPassword
import com.example.lockband.utils.readEncryptedFile
import com.example.lockband.utils.writeEncryptedFile
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

/**
 * Fragment allowing to change password
 */
@AndroidEntryPoint
class ChangePasswordFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        context ?: return binding.root

        val oldPassHash = readEncryptedFile(requireContext().applicationContext, PASS_FILE)

        binding.changePassButton.setOnClickListener {

            binding.wrongOldPassword.visibility = View.GONE
            binding.passwordsMatchingError.visibility = View.GONE

            if (oldPassHash == hashPassword(binding.oldPass.text.toString())) {

                if (binding.newPass.text.toString() == binding.passRepeated.text.toString()) {

                    try {
                        File(requireContext().applicationContext.dataDir, PASS_FILE).delete()

                        writeEncryptedFile(
                            requireContext().applicationContext,
                            PASS_FILE,
                            hashPassword(binding.newPass.text.toString())
                        )
                        Toast.makeText(requireContext(), "Operation successful", Toast.LENGTH_LONG)
                            .show()
                        clearFields(binding)
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            "Operation failed: " + e.message,
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }


                } else {
                    binding.passwordsMatchingError.visibility = View.VISIBLE
                }
            } else {
                binding.wrongOldPassword.visibility = View.VISIBLE
            }
        }

        return binding.root
    }

    private fun clearFields(binding: FragmentChangePasswordBinding) {
        binding.oldPass.text.clear()
        binding.newPass.text.clear()
        binding.passRepeated.text.clear()
    }
}