package ir.dorehami.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ir.dorehami.app.net.Api
import ir.dorehami.app.net.ApiResult
import kotlinx.coroutines.launch
import org.json.JSONObject

private fun errorText(f: ApiResult.Fail): String = when (f.code) {
    "validation_error" -> "اطلاعات واردشده را بررسی کنید."
    else -> f.message
}

private fun userName(json: JSONObject): String {
    val u = json.optJSONObject("user") ?: return ""
    return (u.optString("firstName") + " " + u.optString("lastName")).trim()
}

private fun handleAuth(r: ApiResult, onOk: (String, String, String) -> Unit, onErr: (String) -> Unit) {
    when (r) {
        is ApiResult.Ok -> {
            val t = r.json.optJSONObject("tokens")
            if (t == null) onErr("پاسخ سرور نامعتبر بود.")
            else onOk(t.getString("accessToken"), t.getString("refreshToken"), userName(r.json))
        }
        is ApiResult.Fail -> onErr(errorText(r))
    }
}

@Composable
fun LoginScreen(onLoggedIn: (String, String, String) -> Unit, onRegister: () -> Unit) {
    var id by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("دورهمی", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(id, { id = it }, label = { Text("نام کاربری یا شماره موبایل") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(pass, { pass = it }, label = { Text("رمز عبور") }, singleLine = true, visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
        Spacer(Modifier.height(16.dp))
        Button(
            enabled = !busy && id.isNotBlank() && pass.isNotEmpty(),
            onClick = {
                busy = true; error = null
                scope.launch {
                    val trimmed = id.trim()
                    val isPhone = trimmed.firstOrNull()?.let { it.isDigit() || it == '+' } == true
                    val body = JSONObject().put(if (isPhone) "phone" else "username", trimmed).put("password", pass)
                    handleAuth(Api.post("/auth/login", body), onLoggedIn) { error = it }
                    busy = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) { Text(if (busy) "…" else "ورود") }
        TextButton(onClick = onRegister) { Text("ثبت‌نام") }
    }
}

@Composable
fun RegisterScreen(onLoggedIn: (String, String, String) -> Unit, onBack: () -> Unit) {
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var birth by remember { mutableStateOf("") }
    var insta by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("male") }
    var terms by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val full = Modifier.fillMaxWidth()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("ثبت‌نام", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(first, { first = it }, label = { Text("نام") }, singleLine = true, modifier = full)
        OutlinedTextField(last, { last = it }, label = { Text("نام خانوادگی") }, singleLine = true, modifier = full)
        OutlinedTextField(username, { username = it }, label = { Text("نام کاربری (انگلیسی)") }, singleLine = true, modifier = full)
        OutlinedTextField(phone, { phone = it }, label = { Text("شماره موبایل") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = full)
        OutlinedTextField(pass, { pass = it }, label = { Text("رمز عبور (حداقل ۸ نویسه)") }, singleLine = true, visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = full)
        OutlinedTextField(birth, { birth = it }, label = { Text("تاریخ تولد میلادی (مثلاً 2000-05-21)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = full)
        OutlinedTextField(insta, { insta = it }, label = { Text("آیدی اینستاگرام (اختیاری)") }, singleLine = true, modifier = full)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = full) {
            RadioButton(gender == "male", { gender = "male" }); Text("مرد")
            Spacer(Modifier.width(16.dp))
            RadioButton(gender == "female", { gender = "female" }); Text("زن")
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = full) {
            Checkbox(terms, { terms = it }); Text("قوانین را می‌پذیرم")
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(12.dp))
        Button(
            enabled = !busy && terms && first.isNotBlank() && last.isNotBlank() && username.isNotBlank() && phone.isNotBlank() && pass.length >= 8 && birth.isNotBlank(),
            onClick = {
                busy = true; error = null
                scope.launch {
                    val body = JSONObject()
                        .put("firstName", first.trim()).put("lastName", last.trim())
                        .put("username", username.trim()).put("phone", phone.trim())
                        .put("password", pass).put("gender", gender).put("birthDate", birth.trim())
                        .put("acceptTerms", true)
                    if (insta.isNotBlank()) body.put("instagramId", insta.trim())
                    handleAuth(Api.post("/auth/register", body), onLoggedIn) { error = it }
                    busy = false
                }
            },
            modifier = full.height(48.dp),
        ) { Text(if (busy) "…" else "ثبت‌نام") }
        TextButton(onClick = onBack) { Text("بازگشت") }
    }
}
