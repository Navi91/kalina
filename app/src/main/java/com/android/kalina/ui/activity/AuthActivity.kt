package com.android.kalina.ui.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import com.jakewharton.rxbinding.widget.RxTextView
import com.android.kalina.R
import com.android.kalina.api.auth.WrongCodeApiException
import com.android.kalina.ui.Studio21Activity
import com.android.kalina.viewmodel.auth.AuthState
import com.android.kalina.viewmodel.auth.AuthViewModel
import kotlinx.android.synthetic.main.a_auth.*
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.slots.PredefinedSlots
import ru.tinkoff.decoro.slots.Slot
import ru.tinkoff.decoro.watchers.MaskFormatWatcher
import rx.Observable
import rx.Subscription

class AuthActivity : Studio21Activity() {

    private val PHONE_LENGTH = 11

    private val PHONE_NUMBER_SLOTS = arrayOf(PredefinedSlots.hardcodedSlot('+'),
            PredefinedSlots.digit(),
            PredefinedSlots.hardcodedSlot(' ').withTags(Slot.TAG_DECORATION),
            PredefinedSlots.hardcodedSlot('(').withTags(Slot.TAG_DECORATION),
            PredefinedSlots.digit(),
            PredefinedSlots.digit(),
            PredefinedSlots.digit(),
            PredefinedSlots.hardcodedSlot(')').withTags(Slot.TAG_DECORATION),
            PredefinedSlots.hardcodedSlot(' ').withTags(Slot.TAG_DECORATION),
            PredefinedSlots.digit(),
            PredefinedSlots.digit(),
            PredefinedSlots.digit(),
            PredefinedSlots.hardcodedSlot('-').withTags(Slot.TAG_DECORATION),
            PredefinedSlots.digit(),
            PredefinedSlots.digit(),
            PredefinedSlots.hardcodedSlot('-').withTags(Slot.TAG_DECORATION),
            PredefinedSlots.digit(),
            PredefinedSlots.digit(),
            PredefinedSlots.digit(),
            PredefinedSlots.digit())

    private val phoneMask = MaskImpl.createNonTerminated(PHONE_NUMBER_SLOTS)
    private var phoneActionDoneSubscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.a_auth)

        initActionBar()
        setPhoneMask()
        setUserAgreementText()

        val viewModel = ViewModelProviders.of(this).get(AuthViewModel::class.java)

        viewModel.getAuthStateData().observe(this, Observer { state ->
            when (state) {
                AuthState.REQUEST_AUTH_CREDENTIAL -> {
                    enterButton.setOnClickListener { requestCode(viewModel) }
                    codeEditText.visibility = View.GONE
                    userAgreementTextView.visibility = View.GONE
                    updateButtonState(state)
                }
                AuthState.REQUEST_AUTH_CODE -> {
                    enterButton.setOnClickListener { confirmCode(viewModel) }
                    sendCodeAgainButton.setOnClickListener { requestCode(viewModel) }
                    codeEditText.visibility = View.VISIBLE
                    userAgreementTextView.visibility = View.VISIBLE
                    updateButtonState(state)

                    phoneActionDoneSubscription?.unsubscribe()

                    codeEditText.requestFocus()
                }
                AuthState.AUTH_DONE -> {
                    val intent = Intent(this, ChatActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        })

        viewModel.getProgressData().observe(this, Observer { progress ->
            if (progress == null) return@Observer

            if (progress) {
                progressLayout.visibility = View.VISIBLE
                setViewsEnable(false, nameEditText, phoneEditText, codeEditText, enterButton, sendCodeAgainButton, userAgreementTextView)
            } else {
                progressLayout.visibility = View.GONE
                setViewsEnable(true, nameEditText, phoneEditText, codeEditText, enterButton, sendCodeAgainButton, userAgreementTextView)
            }
        })

        viewModel.getErrorData().observe(this, Observer { error ->
            if (error != null) {
                if (error is WrongCodeApiException) {
                    showErrorToast(getString(R.string.error_wrong_code))
                } else {
                    showErrorToast(error.message)
                }
            }
        })

        createTextChangesObservable(nameEditText).subscribe { viewModel.setName(it) }
        createTextChangesObservable(codeEditText).subscribe { viewModel.setCode(it) }
        createTextChangesObservable(phoneEditText).map { getEnteredPhoneWithoutMask(it) }.subscribe { viewModel.setPhone(it) }

        phoneActionDoneSubscription = RxTextView.editorActionEvents(phoneEditText)
                .filter { it.actionId() == EditorInfo.IME_ACTION_DONE }
                .subscribe { requestCode(viewModel) }
        RxTextView.editorActionEvents(codeEditText)
                .filter { it.actionId() == EditorInfo.IME_ACTION_DONE }
                .subscribe { confirmCode(viewModel) }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return false
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun requestCode(viewModel: AuthViewModel) {
        viewModel.requestCode()
    }

    private fun confirmCode(viewModel: AuthViewModel) {
        viewModel.confirmCode()
    }

    private fun createTextChangesObservable(editText: EditText): Observable<String> {
        return RxTextView.textChanges(editText).map { it.toString() }
    }

    private fun getEnteredPhoneWithoutMask(phoneWithMask: String): String {
        val mask = MaskImpl.createNonTerminated(PHONE_NUMBER_SLOTS)
        mask.insertFront(phoneWithMask)
        val phone = mask.toUnformattedString()

        return phone.substring(1)
    }

    private fun updateButtonState(state: AuthState) {
        val nameAndPhoneNotEmpty = getEnteredPhoneWithoutMask(phoneEditText.text.toString()).length >= PHONE_LENGTH && nameEditText.text.isNotEmpty()

        if (state == AuthState.REQUEST_AUTH_CREDENTIAL) {
            enterButton.isEnabled = nameAndPhoneNotEmpty
            enterButton.setText(getString(R.string.take_code_caps))
            sendCodeAgainButton.visibility = View.INVISIBLE
        } else {
            enterButton.isEnabled = nameAndPhoneNotEmpty and codeEditText.text.isNotEmpty()
            enterButton.setText(getString(R.string.confirm_caps))
            sendCodeAgainButton.visibility = View.VISIBLE
        }
    }

    private fun setPhoneMask() {
        phoneMask.isHideHardcodedHead = false
        phoneMask.insertFront("7")
        val watcher = MaskFormatWatcher(phoneMask)
        watcher.installOnAndFill(phoneEditText)
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        val chevronImage = VectorDrawableCompat.create(resources, R.drawable.ic_chevron_left_white_24dp, theme)
        chevronImage?.setTint(ContextCompat.getColor(this, R.color.colorAccent))
        supportActionBar?.setHomeAsUpIndicator(chevronImage)
    }

    private fun setUserAgreementText() {
        val userAgreement = getString(R.string.user_agreement_in_message)
        val userAgreementMessage = getString(R.string.user_agreement_message, userAgreement)

        val spannableString = SpannableString(userAgreementMessage)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(p0: View?) {
                startActivity(Intent(this@AuthActivity, UserAgreementActivity::class.java))
            }

            override fun updateDrawState(ds: TextPaint?) {
                super.updateDrawState(ds)
                ds?.isUnderlineText = true
            }
        }
        spannableString.setSpan(clickableSpan, userAgreementMessage.length - userAgreement.length, userAgreementMessage.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        userAgreementTextView.text = spannableString
        userAgreementTextView.movementMethod = LinkMovementMethod.getInstance()
        userAgreementTextView.setLinkTextColor(ContextCompat.getColor(this, R.color.colorAccent))
        userAgreementTextView.highlightColor = Color.TRANSPARENT
    }

    private fun setViewsEnable(enable: Boolean, vararg views: View) {
        views.forEach {
            it.isEnabled = enable
        }
    }

    private fun showErrorToast(message: String?) {
        showToast(message, Toast.LENGTH_LONG)
    }
}