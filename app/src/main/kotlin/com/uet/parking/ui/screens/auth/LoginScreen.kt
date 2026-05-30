package com.uet.parking.ui.screens.auth

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.uet.parking.data.model.User
import com.uet.parking.data.model.UserInfo
import com.uet.parking.data.model.enums.UserRole
import com.uet.parking.data.repository.ParkingRepository
import com.uet.parking.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginScreen(
    repository: ParkingRepository,
    onLoginSuccess: (String, UserRole) -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("parking_prefs", Context.MODE_PRIVATE) }
    
    var loginType by remember { mutableStateOf<LoginMethod?>(null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)!!
            val gEmail = account.email ?: ""
            Log.d("Auth", "Google account retrieved: $gEmail")
            
            if (!gEmail.endsWith("@vnu.edu.vn", ignoreCase = true)) {
                errorText = "Vui lòng sử dụng mail @vnu.edu.vn"
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(com.uet.parking.R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                GoogleSignIn.getClient(context, gso).signOut().addOnCompleteListener {
                    auth.signOut()
                    isLoading = false
                }
                return@rememberLauncherForActivityResult
            }

            val idToken = account.idToken
            if (idToken == null) {
                errorText = "Không lấy được ID Token từ Google"
                isLoading = false
                return@rememberLauncherForActivityResult
            }

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            scope.launch {
                try {
                    val authResult = auth.signInWithCredential(credential).await()
                    val firebaseUser: FirebaseUser? = authResult.user
                    
                    if (firebaseUser != null) {
                        Log.d("Auth", "Firebase auth success: ${firebaseUser.uid}")

                        val existingUser = repository.getUserByEmail(gEmail)
                        if (existingUser == null) {
                            Log.d("Auth", "Creating new user in Firestore")
                            // Create new user for VNU mail
                            val newUser = User(
                                userId = firebaseUser.uid,
                                email = gEmail,
                                name = account.displayName ?: "VNU Student",
                                role = UserRole.USER,
                                password = "" // Google login doesn't need password
                            )
                            repository.createUser(newUser)
                            repository.createUserInfo(UserInfo(userId = firebaseUser.uid, debt = 0.0))
                            Toast.makeText(context, "Chào mừng sinh viên mới!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess(firebaseUser.uid, UserRole.USER)
                        } else {
                            if (existingUser.role != UserRole.USER) {
                                errorText = "Tài khoản này không phải là sinh viên"
                                auth.signOut()
                            } else {
                                Toast.makeText(context, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                                onLoginSuccess(existingUser.userId ?: firebaseUser.uid, UserRole.USER)
                            }
                        }
                    }
                } catch (e: Exception) {
                    errorText = "Lỗi xác thực Firebase: ${e.message}"
                    Log.e("Auth", "Firebase auth failed", e)
                } finally {
                    isLoading = false
                }
            }
        } catch (e: Exception) {
            val statusCode = (e as? ApiException)?.statusCode
            errorText = when (statusCode) {
                12501 -> "Hủy đăng nhập Google"
                10 -> "Lỗi cấu hình (Developer Error). Vui lòng kiểm tra SHA-1 và Client ID trong Firebase."
                7 -> "Lỗi mạng. Vui lòng kiểm tra kết nối internet."
                else -> "Lỗi đăng nhập Google: ${e.localizedMessage ?: "Không xác định"}"
            }
            Log.e("Auth", "Google sign in result failed. Code: $statusCode", e)
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Aesthetic gradient mesh background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(PrimaryBlue.copy(alpha = 0.05f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(0f, 0f),
                        radius = 2000f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthHeader()
            
            Spacer(modifier = Modifier.height(48.dp))

            Surface(
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = SurfaceContainerLowest,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, OutlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (loginType == null) {
                        Text(
                            text = "Chào mừng bạn",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Text(
                            text = "Vui lòng chọn phương thức đăng nhập",
                            fontSize = 14.sp,
                            color = OnSecondaryContainer,
                            modifier = Modifier.padding(top = 8.dp),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))

                        LoginActionButton(
                            text = "Đăng nhập bằng mail VNU",
                            painter = androidx.compose.ui.res.painterResource(id = com.uet.parking.R.drawable.vnu_logo),
                            containerColor = PrimaryBlue,
                            contentColor = Color.White,
                            onClick = {
                                try {
                                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestIdToken(context.getString(com.uet.parking.R.string.default_web_client_id))
                                        .requestEmail()
                                        .build()
                                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                    googleSignInClient.signOut().addOnCompleteListener {
                                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                        isLoading = true
                                    }
                                } catch (e: Exception) {
                                    errorText = "Lỗi khởi tạo Google: ${e.message}"
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LoginActionButton(
                            text = "Đăng nhập bằng tài khoản",
                            painter = androidx.compose.ui.res.painterResource(id = com.uet.parking.R.drawable.vnu_logo),
                            containerColor = PrimaryBlue,
                            contentColor = Color.White,
                            onClick = { 
                                loginType = LoginMethod.ACCOUNT 
                                errorText = ""
                            }
                        )

                        if (errorText.isNotEmpty()) {
                            Text(
                                text = errorText,
                                color = Error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 16.dp).align(Alignment.Start),
                                textAlign = TextAlign.Start
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { 
                                loginType = null
                                errorText = ""
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryBlue)
                            }
                            Text(
                                text = "Đăng nhập",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        
                        Text(
                            text = "Nhập thông tin để tiếp tục",
                            fontSize = 14.sp,
                            color = OnSecondaryContainer,
                            modifier = Modifier.padding(top = 8.dp).align(Alignment.Start),
                            textAlign = TextAlign.Start
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))

                        // Form Mode
                        AuthTextField(
                            value = email,
                            onValueChange = { email = it; errorText = "" },
                            label = "TÀI KHOẢN / EMAIL",
                            placeholder = "Nhập tài khoản hoặc email",
                            icon = Icons.Default.Person
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        AuthTextField(
                            value = password,
                            onValueChange = { password = it; errorText = "" },
                            label = "MẬT KHẨU",
                            placeholder = "••••••••",
                            icon = Icons.Default.Lock,
                            isPass = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Move "Quên mật khẩu" before Login Button
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            Text(
                                text = "Quên mật khẩu?",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlue,
                                modifier = Modifier.clickable { 
                                    Toast.makeText(context, "Vui lòng liên hệ phòng đào tạo để lấy lại mật khẩu", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                val trimmedEmail = email.trim()
                                if (trimmedEmail.isEmpty() || password.isEmpty()) {
                                    errorText = "Vui lòng điền đầy đủ thông tin"
                                    return@Button
                                }
                                
                                scope.launch {
                                    isLoading = true
                                    try {
                                        val user = repository.getUserByEmail(trimmedEmail)
                                        if (user != null && user.password == password) {
                                            // LƯU PHIÊN ĐĂNG NHẬP VÀO MÁY
                                            sharedPrefs.edit().apply {
                                                putString("logged_in_user_id", user.userId)
                                                putString("user_role", user.role.name)
                                                apply()
                                            }

                                            Toast.makeText(context, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                                            onLoginSuccess(user.userId ?: "", user.role)
                                        } else {
                                            errorText = "Sai tài khoản hoặc mật khẩu"
                                        }
                                    } catch (e: Exception) {
                                        errorText = "Lỗi kết nối: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Đăng nhập", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        if (errorText.isNotEmpty()) {
                            Text(
                                text = errorText,
                                color = Error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 16.dp).align(Alignment.Start),
                                textAlign = TextAlign.Start
                            )
                        }
                    }

                    if (isLoading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.size(24.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Spacer(modifier = Modifier.weight(1f))
            FooterLegal()
        }

        // Floating Help Button
        FloatingActionButton(
            onClick = { 
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://uet.vnu.edu.vn/"))
                context.startActivity(intent)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = PrimaryBlue,
            contentColor = OnPrimary,
            shape = androidx.compose.foundation.shape.CircleShape
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = "Help"
            )
        }
    }
}

enum class LoginMethod { ACCOUNT }

@Composable
fun LoginActionButton(
    text: String,
    painter: androidx.compose.ui.graphics.painter.Painter,
    containerColor: Color,
    contentColor: Color,
    border: BorderStroke? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(60.dp), // Tăng chiều cao nút một chút để logo to hơn
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = border,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
    ) {
        androidx.compose.foundation.Image(
            painter = painter, 
            contentDescription = null, 
            modifier = Modifier.height(28.dp).widthIn(max = 60.dp),
            contentScale = androidx.compose.ui.layout.ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(showBackground = true)
@Composable
fun LoginActionButtonPreview() {
    LoginActionButton(
        text = "Đăng nhập bằng mail VNU",
        painter = androidx.compose.ui.res.painterResource(id = com.uet.parking.R.drawable.vnu_logo),
        containerColor = Color(0xFF0D47A1),
        contentColor = Color.White,
        onClick = {}
    )
}
