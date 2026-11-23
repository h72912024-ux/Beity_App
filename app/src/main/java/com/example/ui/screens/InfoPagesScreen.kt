package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.BeityLogo
import com.example.ui.components.BeityLogoVariant
import com.example.ui.theme.BeityGold
import com.example.ui.theme.BeityNavy
import com.example.ui.theme.BeityWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoPagesScreen(
    pageKey: String,
    onBack: () -> Unit,
    onGoHome: () -> Unit,
    onSubmitTicket: (reportType: String, reason: String, details: String, onDone: () -> Unit) -> Unit
) {
    val pageTitle = when (pageKey) {
        "about" -> "من نحن"
        "contact" -> "اتصل بنا"
        "faq" -> "الأسئلة الشائعة"
        "privacy" -> "سياسة الخصوصية"
        "terms" -> "الشروط والأحكام"
        "publishing" -> "سياسة نشر الإعلانات"
        "report_issue" -> "الإبلاغ عن مشكلة"
        else -> "الصفحة غير موجودة (404)"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pageTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BeityNavy,
                    titleContentColor = BeityWhite,
                    navigationIconContentColor = BeityWhite
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (pageKey) {
                "about" -> {
                    BeityLogo(variant = BeityLogoVariant.ON_LIGHT)
                    InfoSectionCard(
                        title = "رؤية بيتي | Beity",
                        body = "منصة بيتي هي بوابتك العقارية الحديثة المتخصصة في بيع وشراء وتأجير العقارات في السوق المصري والعربي. نجمع بين الفخامة، الشفافية، وأدوات البحث والخرائط التفاعلية الذكية لتجعل رحلة البحث عن منزلك القادم أو تسويق عقارك أسهل وأكثر أماناً."
                    )
                    InfoSectionCard(
                        title = "قيمنا",
                        body = "• مراجعة دقيقة لجميع الإعلانات قبل النشر.\n• تواصل مباشر وآمن بين المشتري والمعلن.\n• خرائط ومقارنات احترافية تساعدك على اتخاذ القرار الصحيح."
                    )
                }
                "contact", "report_issue" -> {
                    var subject by remember { mutableStateOf("") }
                    var message by remember { mutableStateOf("") }
                    val isIssue = pageKey == "report_issue"

                    InfoSectionCard(
                        title = if (isIssue) "الإبلاغ عن مشكلة تقنية أو إعلان" else "تواصل مع فريق بيتي",
                        body = "فريق الدعم لدينا متاح للرد على جميع استفساراتكم وملاحظاتكم."
                    )
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text(if (isIssue) "نوع المشكلة *" else "موضوع الرسالة *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("التفاصيل *") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (subject.trim().length >= 2 && message.trim().length >= 5) {
                                onSubmitTicket(
                                    "support_ticket",
                                    subject.trim(),
                                    message.trim()
                                ) {
                                    subject = ""
                                    message = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BeityNavy, contentColor = BeityGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إرسال الرسالة", fontWeight = FontWeight.Bold)
                    }
                }
                "faq" -> {
                    val faqs = listOf(
                        "كيف أضيف عقاراً للبيع أو الإيجار على بيتي؟" to "يمكنك الضغط على زر 'أضف عقارك' في الشريط العلوي، تعبئة بيانات العقار والصور، ثم إرساله ليتم مراجعته ونشره من قِبل الإدارة.",
                        "كم تستغرق مراجعة الإعلان؟" to "تتم مراجعة الإعلانات خلال وقت قصير لضمان صحة البيانات وجودة الصور قبل ظهورها للجمهور.",
                        "هل يمكنني مقارنة أكثر من عقار؟" to "نعم، اضغط على أيقونة الميزان في أي بطاقة عقار لإضافته إلى جدول المقارنة ومقارنة حتى 3 عقارات جنباً إلى جنب.",
                        "كيف أتواصل مع المعلن؟" to "من صفحة تفاصيل العقار يمكنك الاتصال هاتفياً، التواصل عبر واتساب، أو بدء محادثة فورية داخل التطبيق."
                    )
                    faqs.forEach { (q, a) ->
                        FaqAccordionItem(question = q, answer = a)
                    }
                }
                "privacy" -> {
                    InfoSectionCard(
                        title = "سياسة الخصوصية وحماية البيانات",
                        body = "نلتزم في منصة بيتي بحماية خصوصية مستخدمينا. تُحفظ بيانات الحساب الشخصية (مثل البريد الإلكتروني ورقم الهاتف) بشكل آمن ولا تُشارك للعامة إلا في بيانات التواصل التي يختار المعلن عرضها في إعلاناته."
                    )
                }
                "terms" -> {
                    InfoSectionCard(
                        title = "الشروط والأحكام",
                        body = "باستخدامك لتطبيق بيتي فإنك تتعهد بتقديم معلومات صحيحة ودقيقة عند نشر الإعلانات العقارية أو التواصل مع المعلنين، والالتزام باللوائح المنظمة للسوق العقاري."
                    )
                }
                "publishing" -> {
                    InfoSectionCard(
                        title = "سياسة نشر الإعلانات العقارية",
                        body = "• يجب أن يكون الإعلان لعقار حقيقي ومتاح فعلياً.\n• يجب كتابة السعر والمساحة والموقع بدقة.\n• يُمنع نشر صور مضللة أو محتوى مكرر.\n• تحتفظ الإدارة بحق تعديل أو رفض أي إعلان مخالف."
                    )
                }
                else -> {
                    // 404 Professional Page
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(Icons.Outlined.HomeWork, contentDescription = null, tint = BeityGold, modifier = Modifier.size(64.dp))
                        Text("404", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold, color = BeityNavy)
                        Text("عذراً، الصفحة المطلوبة غير متوفرة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("قد يكون الرابط قديم أو تم نقل الصفحة.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        Button(
                            onClick = onGoHome,
                            colors = ButtonDefaults.buttonColors(containerColor = BeityNavy, contentColor = BeityGold)
                        ) { Text("العودة إلى الرئيسية") }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoSectionCard(title: String, body: String) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BeityGold)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun FaqAccordionItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(question, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Icon(
                    if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = BeityGold
                )
            }
            if (expanded) {
                Text(answer, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
