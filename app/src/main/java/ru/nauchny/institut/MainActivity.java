package ru.nauchny.institut;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    // ============================================================
    // ЦВЕТА
    // ============================================================

    private static final int NAVY = Color.rgb(12, 35, 58);
    private static final int NAVY2 = Color.rgb(20, 55, 87);
    private static final int BLUE = Color.rgb(32, 103, 180);
    private static final int BLUE2 = Color.rgb(69, 137, 211);
    private static final int TEAL = Color.rgb(24, 148, 143);
    private static final int GREEN = Color.rgb(44, 150, 92);
    private static final int RED = Color.rgb(190, 65, 65);
    private static final int GOLD = Color.rgb(194, 142, 38);

    private static final int BG = Color.rgb(244, 247, 250);
    private static final int CARD = Color.WHITE;
    private static final int TEXT = Color.rgb(24, 37, 50);
    private static final int MUTED = Color.rgb(92, 108, 122);
    private static final int BORDER = Color.rgb(220, 228, 236);

    // ============================================================
    // НАСТРОЙКИ
    // ============================================================

    private static final String PREFS_AI = "ai_settings";

    private static final int PICK_DOCUMENT = 1001;

    // ============================================================
    // ДАННЫЕ
    // ============================================================

    private DB db;
    private SharedPreferences prefs;

    private final ArrayList<Department> departments = new ArrayList<>();
    private final ArrayList<Agent> agents = new ArrayList<>();
    private final ArrayList<Pipeline> pipelines = new ArrayList<>();
    private final ArrayList<Course> courses = new ArrayList<>();
    private final ArrayList<DocumentItem> documents = new ArrayList<>();

    // ============================================================
    // AI ПРОВАЙДЕРЫ
    // ============================================================

    private final ArrayList<AIProvider> providers = new ArrayList<>();

    // ============================================================
    // МОДЕЛИ
    // ============================================================

    static class Department {
        long id;
        String name;
        String description;

        Department(String name, String description) {
            this.name = name;
            this.description = description;
        }

        Department(long id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
    }

    static class Agent {
        long id;
        String name;
        String role;
        String department;
        String qualification = "Не определена";
        String competencies = "";

        Agent(String name, String role, String department) {
            this.name = name;
            this.role = role;
            this.department = department;
        }

        Agent(long id, String name, String role, String department,
              String qualification, String competencies) {
            this.id = id;
            this.name = name;
            this.role = role;
            this.department = department;
            this.qualification = qualification;
            this.competencies = competencies;
        }
    }

    static class Pipeline {
        long id;
        String title;
        String type;
        String pages;
        String standard;
        String deadline;
        String field;
        String web;
        String review;
        String status = "Создано";

        Pipeline(String title,
                 String type,
                 String pages,
                 String standard,
                 String deadline,
                 String field,
                 String web,
                 String review) {

            this.title = title;
            this.type = type;
            this.pages = pages;
            this.standard = standard;
            this.deadline = deadline;
            this.field = field;
            this.web = web;
            this.review = review;
        }

        Pipeline(long id,
                 String title,
                 String type,
                 String pages,
                 String standard,
                 String deadline,
                 String field,
                 String web,
                 String review,
                 String status) {

            this.id = id;
            this.title = title;
            this.type = type;
            this.pages = pages;
            this.standard = standard;
            this.deadline = deadline;
            this.field = field;
            this.web = web;
            this.review = review;
            this.status = status;
        }
    }

    static class Course {
        long id;
        String title;
        String material;
        String status = "Назначен";

        Course(String title, String material) {
            this.title = title;
            this.material = material;
        }

        Course(long id, String title, String material, String status) {
            this.id = id;
            this.title = title;
            this.material = material;
            this.status = status;
        }
    }

    static class DocumentItem {
        long id;
        String name;
        String uri;

        DocumentItem(String name, String uri) {
            this.name = name;
            this.uri = uri;
        }

        DocumentItem(long id, String name, String uri) {
            this.id = id;
            this.name = name;
            this.uri = uri;
        }
    }

    static class AIProvider {
        String id;
        String name;
        String defaultModel;
        String baseUrl;
        boolean local;

        AIProvider(String id,
                   String name,
                   String defaultModel,
                   String baseUrl,
                   boolean local) {

            this.id = id;
            this.name = name;
            this.defaultModel = defaultModel;
            this.baseUrl = baseUrl;
            this.local = local;
        }
    }

    // ============================================================
    // ЖИЗНЕННЫЙ ЦИКЛ
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(NAVY);
        getWindow().setNavigationBarColor(NAVY);

        db = new DB(this);
        prefs = getSharedPreferences(PREFS_AI, MODE_PRIVATE);

        initProviders();
        loadData();
        seedIfNeeded();

        showHome();
    }

    // ============================================================
    // ПРОВАЙДЕРЫ
    // ============================================================

    private void initProviders() {

        providers.clear();

        providers.add(new AIProvider(
                "gemini",
                "Google Gemini",
                "gemini-2.5-flash",
                "https://generativelanguage.googleapis.com/v1beta/models/",
                false
        ));

        providers.add(new AIProvider(
                "openai",
                "OpenAI GPT",
                "gpt-4o-mini",
                "https://api.openai.com/v1/chat/completions",
                false
        ));

        providers.add(new AIProvider(
                "deepseek",
                "DeepSeek",
                "deepseek-chat",
                "https://api.deepseek.com/chat/completions",
                false
        ));

        providers.add(new AIProvider(
                "grok",
                "Grok / xAI",
                "grok-3-mini",
                "https://api.x.ai/v1/chat/completions",
                false
        ));

        providers.add(new AIProvider(
                "kimi",
                "Kimi / Moonshot",
                "kimi-k2",
                "https://api.moonshot.ai/v1/chat/completions",
                false
        ));

        providers.add(new AIProvider(
                "qwen",
                "Qwen / Alibaba",
                "qwen-plus",
                "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions",
                false
        ));

        providers.add(new AIProvider(
                "local",
                "Локальный Qwen3 8B",
                "Qwen3-8B-Q4_K_M.gguf",
                "http://127.0.0.1:8080/v1/chat/completions",
                true
        ));
    }

    // ============================================================
    // БАЗОВЫЙ UI
    // ============================================================

    private LinearLayout root() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        return root;
    }

    private ScrollView scroll(View child) {
        ScrollView s = new ScrollView(this);
        s.setFillViewport(true);
        s.setBackgroundColor(BG);
        s.addView(child);
        return s;
    }

    private LinearLayout content() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(18), dp(14), dp(18), dp(28));
        return l;
    }

    private TextView text(String value, float size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(TEXT);
        t.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        return t;
    }

    private TextView title(String value) {
        TextView t = text(value, 27);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setTextColor(NAVY);
        t.setPadding(0, dp(5), 0, dp(5));
        return t;
    }

    private TextView subtitle(String value) {
        TextView t = text(value, 15);
        t.setTextColor(MUTED);
        t.setPadding(0, dp(2), 0, dp(8));
        return t;
    }

    private Button button(String value) {
        Button b = new Button(this);
        b.setText(value);
        b.setTextSize(15);
        b.setTextColor(TEXT);
        b.setAllCaps(false);
        b.setMinHeight(dp(50));
        b.setPadding(dp(16), 0, dp(16), 0);
        return b;
    }

    private Button primaryButton(String value) {
        Button b = button(value);
        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(BLUE);
        return b;
    }

    private TextView cardTitle(String value) {
        TextView t = text(value, 18);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setTextColor(NAVY);
        return t;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16), dp(14), dp(16), dp(14));
        c.setBackgroundColor(CARD);
        c.setElevation(dp(2));
        return c;
    }

    private void addGap(LinearLayout l, int h) {
        Space s = new Space(this);
        l.addView(s, new LinearLayout.LayoutParams(
                1,
                dp(h)
        ));
    }

    private void addButton(LinearLayout parent, String label, View.OnClickListener listener) {
        Button b = button(label);
        b.setOnClickListener(listener);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(54)
                );

        p.setMargins(0, dp(5), 0, dp(5));

        parent.addView(b, p);
    }

    private void addPrimaryButton(LinearLayout parent, String label, View.OnClickListener listener) {
        Button b = primaryButton(label);
        b.setOnClickListener(listener);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(54)
                );

        p.setMargins(0, dp(5), 0, dp(5));

        parent.addView(b, p);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    // ============================================================
    // ВЕРХНЯЯ ПАНЕЛЬ
    // ============================================================

    private LinearLayout header(String section) {

        LinearLayout h = new LinearLayout(this);
        h.setOrientation(LinearLayout.HORIZONTAL);
        h.setGravity(Gravity.CENTER_VERTICAL);
        h.setPadding(dp(18), dp(10), dp(18), dp(10));
        h.setBackgroundColor(Color.WHITE);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);

        TextView app = text("НАУЧНЫЙ ИНСТИТУТ", 13);
        app.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        app.setTextColor(BLUE);

        TextView sec = text(section, 22);
        sec.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        sec.setTextColor(NAVY);

        texts.addView(app);
        texts.addView(sec);

        h.addView(texts, new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        ));

        Button home = button("Главная");
        home.setOnClickListener(v -> showHome());

        h.addView(home, new LinearLayout.LayoutParams(
                dp(110),
                dp(52)
        ));

        return h;
    }

    // ============================================================
    // HOME
    // ============================================================

    private void showHome() {

        LinearLayout root = root();

        root.addView(header("Ректорат"));

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = content();

        TextView welcome = title("Ректорат");
        body.addView(welcome);

        TextView info = subtitle(
                "Мультиагентный научно-исследовательский институт\n" +
                "Научное производство • агенты • исследования • редакция"
        );
        body.addView(info);

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);

        addStat(stats, "Кафедры", String.valueOf(departments.size()));
        addStat(stats, "Агенты", String.valueOf(agents.size()));
        addStat(stats, "Проекты", String.valueOf(pipelines.size()));

        body.addView(stats);

        addGap(body, 14);

        addSectionCard(
                body,
                "🏛 Кафедры",
                "Научные подразделения института",
                departments.size(),
                v -> showDepartments()
        );

        addSectionCard(
                body,
                "🧠 Научные агенты",
                "Исследователи, методологи, рецензенты и редакторы",
                agents.size(),
                v -> showAgents()
        );

        addSectionCard(
                body,
                "🌐 Web Research",
                "Поиск и анализ внешней научной информации",
                0,
                v -> showWebResearch()
        );

        addSectionCard(
                body,
                "🏆 Соревнования агентов",
                "Сравнение результатов нескольких научных агентов",
                0,
                v -> showCompetitions()
        );

        addSectionCard(
                body,
                "🎓 Академия",
                "Повышение квалификации и обучение агентов",
                courses.size(),
                v -> showAcademy()
        );

        addSectionCard(
                body,
                "📚 Документы института",
                "Материалы ректора, PDF, DOCX и другие источники",
                documents.size(),
                v -> showDocuments()
        );

        addSectionCard(
                body,
                "🤖 AI-оркестратор",
                "Запуск многоэтапного научного конвейера",
                pipelines.size(),
                v -> showPipelines()
        );

        addSectionCard(
                body,
                "⚖ Нормативная база",
                "Нормативные документы и требования к научной работе",
                0,
                v -> showNormative()
        );

        addSectionCard(
                body,
                "🧪 Научное производство",
                "Статьи, НИР, обзорные справки и другие продукты",
                0,
                v -> showProduction()
        );

        addSectionCard(
                body,
                "🧠 Тест локального Qwen3 8B",
                "Проверка модели, работающей непосредственно на телефоне",
                0,
                v -> testLocalQwen()
        );

        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        setContentView(root);
    }

    private void addStat(LinearLayout parent, String name, String value) {

        LinearLayout card = card();
        card.setGravity(Gravity.CENTER);

        TextView number = text(value, 27);
        number.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        number.setTextColor(BLUE);
        number.setGravity(Gravity.CENTER);

        TextView label = text(name, 12);
        label.setTextColor(MUTED);
        label.setGravity(Gravity.CENTER);

        card.addView(number);
        card.addView(label);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        0,
                        dp(90),
                        1
                );

        p.setMargins(dp(3), 0, dp(3), 0);

        parent.addView(card, p);
    }

    private void addSectionCard(
            LinearLayout body,
            String title,
            String description,
            int count,
            View.OnClickListener listener) {

        LinearLayout c = card();

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);

        TextView t = cardTitle(title);
        TextView d = subtitle(description);

        texts.addView(t);
        texts.addView(d);

        row.addView(texts, new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        ));

        if (count > 0) {
            TextView n = text(String.valueOf(count), 18);
            n.setTextColor(BLUE);
            n.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            row.addView(n);
        }

        TextView arrow = text("›", 32);
        arrow.setTextColor(BLUE);
        arrow.setGravity(Gravity.CENTER);

        row.addView(arrow);

        c.addView(row);
        c.setOnClickListener(listener);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        p.setMargins(0, dp(6), 0, dp(6));

        body.addView(c, p);
    }

    // ============================================================
    // КАФЕДРЫ
    // ============================================================

    private void showDepartments() {

        LinearLayout root = root();
        root.addView(header("Кафедры"));

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = content();

        body.addView(title("Кафедры института"));
        body.addView(subtitle(
                "Подразделения можно расширять без изменения архитектуры приложения."
        ));

        addPrimaryButton(
                body,
                "＋ Создать кафедру",
                v -> addDepartmentDialog()
        );

        addGap(body, 10);

        for (Department d : departments) {

            LinearLayout c = card();

            c.addView(cardTitle(d.name));
            c.addView(subtitle(d.description));

            Button details = button("Открыть кафедру");
            details.setOnClickListener(v ->
                    showDepartmentDetails(d)
            );

            c.addView(details);

            body.addView(c, marginParams());
        }

        scroll.addView(body);

        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        setContentView(root);
    }

    private void showDepartmentDetails(Department d) {

        StringBuilder sb = new StringBuilder();

        sb.append(d.name)
                .append("\n\n")
                .append(d.description)
                .append("\n\n")
                .append("Агенты кафедры:\n");

        boolean found = false;

        for (Agent a : agents) {
            if (a.department.equals(d.name)) {
                sb.append("• ")
                        .append(a.name)
                        .append(" — ")
                        .append(a.role)
                        .append("\n");
                found = true;
            }
        }

        if (!found) {
            sb.append("Пока нет назначенных агентов.");
        }

        new AlertDialog.Builder(this)
                .setTitle("Кафедра")
                .setMessage(sb.toString())
                .setPositiveButton("Понятно", null)
                .show();
    }

    private void addDepartmentDialog() {

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(5), dp(20), 0);

        EditText name = edit("Название кафедры");
        EditText description = edit("Научное направление");

        form.addView(name);
        form.addView(description);

        new AlertDialog.Builder(this)
                .setTitle("Новая кафедра")
                .setView(form)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Создать", (dialog, which) -> {

                    String n = name.getText().toString().trim();
                    String d = description.getText().toString().trim();

                    if (n.isEmpty()) {
                        toast("Введите название кафедры");
                        return;
                    }

                    long id = db.dep(n, d);

                    departments.add(
                            new Department(id, n, d)
                    );

                    showDepartments();
                })
                .show();
    }

    // ============================================================
    // АГЕНТЫ
    // ============================================================

    private void showAgents() {

        LinearLayout root = root();
        root.addView(header("Научные агенты"));

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = content();

        body.addView(title("Научные агенты"));
        body.addView(subtitle(
                "Каждый агент имеет роль, кафедру, квалификацию и набор компетенций."
        ));

        addPrimaryButton(
                body,
                "＋ Создать научного агента",
                v -> addAgentDialog()
        );

        for (Agent a : agents) {

            LinearLayout c = card();

            c.addView(cardTitle(a.name));

            TextView role = text(
                    a.role + "\n" + a.department,
                    14
            );

            role.setTextColor(MUTED);

            c.addView(role);

            addGap(c, 8);

            TextView q = text(
                    "Квалификация: " + a.qualification,
                    14
            );

            q.setTextColor(TEXT);

            c.addView(q);

            TextView comp = text(
                    "Компетенции: " +
                            (a.competencies.isEmpty()
                                    ? "не определены"
                                    : a.competencies),
                    14
            );

            comp.setTextColor(MUTED);

            c.addView(comp);

            Button open = button("Профиль агента");
            open.setOnClickListener(v ->
                    showAgentProfile(a)
            );

            c.addView(open);

            body.addView(c, marginParams());
        }

        scroll.addView(body);

        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        setContentView(root);
    }

    private void addAgentDialog() {

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), 0, dp(20), 0);

        EditText name = edit("Имя агента");
        EditText role = edit("Роль");

        Spinner spinner = new Spinner(this);

        ArrayList<String> names = new ArrayList<>();

        for (Department d : departments) {
            names.add(d.name);
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        names
                );

        spinner.setAdapter(adapter);

        form.addView(name);
        form.addView(role);
        form.addView(spinner);

        new AlertDialog.Builder(this)
                .setTitle("Новый научный агент")
                .setView(form)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Создать", (dialog, which) -> {

                    String n = name.getText().toString().trim();
                    String r = role.getText().toString().trim();

                    if (n.isEmpty()) {
                        toast("Введите имя");
                        return;
                    }

                    String dep =
                            departments.isEmpty()
                                    ? "Без кафедры"
                                    : spinner.getSelectedItem().toString();

                    long id = db.agent(
                            n,
                            r,
                            dep,
                            "Начальная квалификация",
                            ""
                    );

                    agents.add(
                            new Agent(
                                    id,
                                    n,
                                    r,
                                    dep,
                                    "Начальная квалификация",
                                    ""
                            )
                    );

                    showAgents();
                })
                .show();
    }

    private void showAgentProfile(Agent a) {

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), 0, dp(20), 0);

        TextView header = cardTitle(a.name);
        form.addView(header);

        form.addView(subtitle(
                a.role + "\n" + a.department
        ));

        EditText qualification =
                edit("Квалификация");

        qualification.setText(a.qualification);

        EditText competencies =
                edit("Компетенции");

        competencies.setText(a.competencies);

        form.addView(qualification);
        form.addView(competencies);

        new AlertDialog.Builder(this)
                .setTitle("Профиль агента")
                .setView(form)
                .setNegativeButton("Закрыть", null)
                .setNeutralButton(
                        "Обучение",
                        (dialog, which) -> showAcademyForAgent(a)
                )
                .setPositiveButton(
                        "Сохранить",
                        (dialog, which) -> {

                            a.qualification =
                                    qualification.getText().toString();

                            a.competencies =
                                    competencies.getText().toString();

                            db.updateAgent(a);

                            showAgents();
                        }
                )
                .show();
    }

    private void showAcademyForAgent(Agent agent) {

        StringBuilder sb = new StringBuilder();

        sb.append("Агент: ")
                .append(agent.name)
                .append("\n\n");

        if (courses.isEmpty()) {
            sb.append("Курсы ещё не созданы.");
        } else {
            for (Course c : courses) {
                sb.append("• ")
                        .append(c.title)
                        .append("\n")
                        .append(c.status)
                        .append("\n\n");
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Повышение квалификации")
                .setMessage(sb.toString())
                .setPositiveButton("Понятно", null)
                .show();
    }

    // ============================================================
    // АКАДЕМИЯ
    // ============================================================

    private void showAcademy() {

        LinearLayout root = root();
        root.addView(header("Академия"));

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = content();

        body.addView(title("Академия / повышение квалификации"));

        body.addView(subtitle(
                "Ректор может передавать агентам ссылки, документы и учебные материалы."
        ));

        addPrimaryButton(
                body,
                "＋ Создать курс",
                v -> addCourseDialog()
        );

        if (courses.isEmpty()) {

            LinearLayout empty = card();

            empty.addView(cardTitle(
                    "Курсов пока нет"
            ));

            empty.addView(subtitle(
                    "Создайте первый курс и назначьте его агентам."
            ));

            body.addView(empty, marginParams());

        } else {

            for (Course c : courses) {

                LinearLayout card = card();

                card.addView(cardTitle(c.title));

                card.addView(
                        subtitle(
                                "Материал: " +
                                        (c.material.isEmpty()
                                                ? "не указан"
                                                : c.material)
                        )
                );

                TextView status =
                        text("Статус: " + c.status, 14);

                status.setTextColor(TEAL);

                card.addView(status);

                body.addView(card, marginParams());
            }
        }

        scroll.addView(body);

        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        setContentView(root);
    }

    private void addCourseDialog() {

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), 0, dp(20), 0);

        EditText title = edit("Название курса");
        EditText material = edit(
                "Ссылка или описание учебного материала"
        );

        form.addView(title);
        form.addView(material);

        new AlertDialog.Builder(this)
                .setTitle("Новый курс")
                .setView(form)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Создать", (dialog, which) -> {

                    String t =
                            title.getText().toString().trim();

                    String m =
                            material.getText().toString().trim();

                    if (t.isEmpty()) {
                        toast("Введите название курса");
                        return;
                    }

                    long id = db.course(
                            t,
                            m,
                            "Назначен"
                    );

                    courses.add(
                            new Course(
                                    id,
                                    t,
                                    m,
                                    "Назначен"
                            )
                    );

                    showAcademy();
                })
                .show();
    }

    // ============================================================
    // ДОКУМЕНТЫ
    // ============================================================

    private void showDocuments() {

        LinearLayout root = root();
        root.addView(header("Документы"));

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = content();

        body.addView(title("Документы института"));

        body.addView(subtitle(
                "Документы ректора могут использоваться как источники для научных агентов."
        ));

        addPrimaryButton(
                body,
                "＋ Загрузить документ",
                v -> pickDocument()
        );

        if (documents.isEmpty()) {

            LinearLayout c = card();

            c.addView(cardTitle(
                    "Документы отсутствуют"
            ));

            c.addView(subtitle(
                    "Поддерживается выбор файлов через системный файловый менеджер Android."
            ));

            body.addView(c, marginParams());

        } else {

            for (DocumentItem d : documents) {

                LinearLayout c = card();

                c.addView(cardTitle(d.name));

                TextView uri = text(
                        d.uri,
                        12
                );

                uri.setTextColor(MUTED);

                c.addView(uri);

                Button open = button("Открыть документ");

                open.setOnClickListener(v -> {
                    try {
                        Intent intent =
                                new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(d.uri)
                                );

                        intent.addFlags(
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

                        startActivity(intent);

                    } catch (Exception e) {
                        toast("Не удалось открыть документ");
                    }
                });

                c.addView(open);

                body.addView(c, marginParams());
            }
        }

        scroll.addView(body);

        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        setContentView(root);
    }

    private void pickDocument() {

        Intent intent =
                new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.setType("*/*");

        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION |
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        );

        startActivityForResult(
                intent,
                PICK_DOCUMENT
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode != PICK_DOCUMENT ||
                resultCode != RESULT_OK ||
                data == null ||
                data.getData() == null) {

            return;
        }

        Uri uri = data.getData();

        try {
            getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (Exception ignored) {
        }

        String name = getFileName(uri);

        long id = db.doc(
                name,
                uri.toString()
        );

        documents.add(
                new DocumentItem(
                        id,
                        name,
                        uri.toString()
                )
        );

        toast("Документ добавлен");

        showDocuments();
    }

    private String getFileName(Uri uri) {

        String result = "Документ";

        try {

            Cursor cursor =
                    getContentResolver().query(
                            uri,
                            null,
                            null,
                            null,
                            null
                    );

            if (cursor != null) {

                int index =
                        cursor.getColumnIndex(
                                OpenableColumns.DISPLAY_NAME
                        );

                if (index >= 0 &&
                        cursor.moveToFirst()) {

                    result =
                            cursor.getString(index);
                }

                cursor.close();
            }

        } catch (Exception ignored) {
        }

        return result;
    }

    // ============================================================
    // AI ОРКЕСТРАТОР
    // ============================================================

    private void showPipelines() {

        LinearLayout root = root();
        root.addView(header("AI-оркестратор"));

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = content();

        body.addView(title("AI-оркестратор"));

        body.addView(subtitle(
                "Научный конвейер распределяет работу между агентами: " +
                        "декомпозиция → исследование → анализ → критик → рецензент → редактор."
        ));

        addPrimaryButton(
                body,
                "＋ Создать научное задание",
                v -> addPipelineDialog()
        );

        if (pipelines.isEmpty()) {

            LinearLayout c = card();

            c.addView(cardTitle(
                    "Заданий пока нет"
            ));

            c.addView(subtitle(
                    "Ректор может создать статью, НИР, обзорную справку или другой научный продукт."
            ));

            body.addView(c, marginParams());

        } else {

            for (Pipeline p : pipelines) {

                LinearLayout c = card();

                c.addView(cardTitle(p.title));

                c.addView(
                        subtitle(
                                "Тип: " + p.type +
                                        "\nОбъём: " + p.pages + " стр." +
                                        "\nОбласть: " + p.field +
                                        "\nСтандарт: " + p.standard +
                                        "\nСрок: " + p.deadline
                        )
                );

                TextView status =
                        text(
                                "Статус: " + p.status,
                                14
                        );

                status.setTextColor(TEAL);

                c.addView(status);

                Button run =
                        primaryButton(
                                "▶ Запустить научный конвейер"
                        );

                run.setOnClickListener(v ->
                        runPipeline(p)
                );

                c.addView(run);

                body.addView(c, marginParams());
            }
        }

        scroll.addView(body);

        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        setContentView(root);
    }

    private void addPipelineDialog() {

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), 0, dp(20), 0);

        EditText title =
                edit("Название исследования");

        EditText type =
                edit(
                        "Тип продукта: статья / НИР / обзорная справка"
                );

        EditText pages =
                edit("Требуемый объём в страницах");

        pages.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
        );

        EditText standard =
                edit(
                        "Требования: ВАК / ГОСТ / иное"
                );

        EditText deadline =
                edit("Срок выполнения");

        EditText field =
                edit("Научная область");

        EditText web =
                edit(
                        "Использовать Web Research? да / нет"
                );

        EditText review =
                edit(
                        "Требуется внешнее рецензирование? да / нет"
                );

        form.addView(title);
        form.addView(type);
        form.addView(pages);
        form.addView(standard);
        form.addView(deadline);
        form.addView(field);
        form.addView(web);
        form.addView(review);

        new AlertDialog.Builder(this)
                .setTitle("Новое научное задание")
                .setView(form)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Создать", (dialog, which) -> {

                    String t =
                            title.getText().toString().trim();

                    if (t.isEmpty()) {
                        toast("Введите тему исследования");
                        return;
                    }

                    Pipeline p =
                            new Pipeline(
                                    t,
                                    type.getText().toString(),
                                    pages.getText().toString(),
                                    standard.getText().toString(),
                                    deadline.getText().toString(),
                                    field.getText().toString(),
                                    web.getText().toString(),
                                    review.getText().toString()
                            );

                    long id = db.pipeline(p);

                    p.id = id;

                    pipelines.add(p);

                    showPipelines();
                })
                .show();
    }

    private void runPipeline(Pipeline p) {

        String prompt =
                "Ты научный агент мультиагентного научно-исследовательского института.\n\n" +
                "Тема исследования: " + p.title + "\n" +
                "Тип продукта: " + p.type + "\n" +
                "Объём: " + p.pages + " страниц\n" +
                "Стандарт: " + p.standard + "\n" +
                "Научная область: " + p.field + "\n\n" +
                "Составь краткий план научного исследования. " +
                "Не выдумывай источники.";

        requestAI(
                prompt,
                "Результат научного конвейера"
        );
    }

    // ============================================================
    // WEB RESEARCH
    // ============================================================

    private void showWebResearch() {

        LinearLayout root = root();
        root.addView(header("Web Research"));

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = content();

        body.addView(title("Web Research"));

        body.addView(subtitle(
                "Модуль предназначен для поиска и анализа актуальной информации из интернета."
        ));

        EditText query =
                edit("Научный запрос");

        body.addView(query);

        addPrimaryButton(
                body,
                "🌐 Исследовать запрос",
                v -> {

                    String q =
                            query.getText().toString().trim();

                    if (q.isEmpty()) {
                        toast("Введите запрос");
                        return;
                    }

                    requestAI(
                            "Проведи предварительный научный анализ темы:\n\n" +
                                    q +
                                    "\n\n" +
                                    "Отдельно укажи, какие источники необходимо проверить через интернет.",
                            "Web Research"
                    );
                }
        );

        LinearLayout c = card();

        c.addView(cardTitle(
                "Научная проверка источников"
        ));

        c.addView(subtitle(
                "В полноценной версии этот модуль будет передавать агентам найденные источники, ссылки и релевантные фрагменты."
        ));

        body.addView(c, marginParams());

        scroll.addView(body);

        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        setContentView(root);
    }

    // ============================================================
    // СОРЕВНОВАНИЯ
    // ============================================================

    private void showCompetitions() {

        LinearLayout root = root();
        root.addView(header("Соревнования"));

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = content();

        body.addView(title("Соревнования агентов"));

        body.addView(subtitle(
                "Несколько агентов получают одну задачу, после чего результаты сравниваются критиками и рецензентами."
        ));

        addPrimaryButton(
                body,
                "＋ Новое соревнование",
                v -> createCompetition()
        );

        LinearLayout c = card();

        c.addView(cardTitle(
                "Механика соревнований"
        ));

        c.addView(subtitle(
                "1. Одна научная задача\n" +
                        "2. Несколько агентов\n" +
                        "3. Независимые ответы\n" +
                        "4. Критик\n" +
                        "5. Рецензент\n" +
                        "6. Итоговый редактор"
        ));

        body.addView(c, marginParams());

        scroll.addView(body);

        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        setContentView(root);
    }

    private void createCompetition() {

        if (agents.size() < 2) {
            toast("Для соревнования нужно минимум два агента");
            return;
        }

        EditText task =
                edit("Научная задача");

        new AlertDialog.Builder(this)
                .setTitle("Новое соревнование")
                .setView(task)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Создать", (dialog, which) -> {

                    String q =
                            task.getText().toString().trim();

                    if (q.isEmpty()) {
                        toast("Введите задачу");
                        return;
                    }

                    StringBuilder prompt =
                            new StringBuilder();

                    prompt.append(
                            "Смоделируй научное соревнование агентов.\n\n"
                    );

                    prompt.append(
                            "Задача:\n"
                    ).append(q).append("\n\n");

                    for (int i = 0; i < agents.size(); i++) {

                        Agent a = agents.get(i);

                        prompt.append(
                                "Агент "
                        ).append(i + 1)
                                .append(": ")
                                .append(a.name)
                                .append(" — ")
                                .append(a.role)
                                .append("\n");
                    }

                    prompt.append(
                            "\nСформируй критерии объективного сравнения."
                    );

                    requestAI(
                            prompt.toString(),
                            "Соревнование агентов"
                    );
                })
                .show();
    }

    // ============================================================
    // НОРМАТИВНАЯ БАЗА
    // ============================================================

    private void showNormative() {

        LinearLayout root = root();
        root.addView(header("Нормативная база"));

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = content();

        body.addView(title("Нормативная база"));

        body.addView(subtitle(
                "Раздел предназначен для хранения и анализа нормативных документов, используемых институтом."
        ));

        addButton(
                body,
                "📄 Федеральные требования к научной деятельности",
                v -> showInfo(
                        "Федеральные требования",
                        "Здесь будет размещаться нормативная база, которую ректор загружает в институт."
                )
        );

        addButton(
                body,
                "📑 Требования к научным публикациям",
                v -> showInfo(
                        "Публикации",
                        "Раздел для требований к оформлению, рецензированию и публикации научных материалов."
                )
        );

        addButton(
                body,
                "🎓 Диссертационные требования",
                v -> showInfo(
                        "Диссертационный совет",
                        "Раздел для нормативных материалов, используемых при подготовке диссертаций."
                )
        );

        addButton(
                body,
                "⚖ Документы, загруженные ректором",
                v -> showDocuments()
        );

        scroll.addView(body);

        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        setContentView(root);
    }

    // ============================================================
    // НАУЧНОЕ ПРОИЗВОДСТВО
    // ============================================================

    private void showProduction() {

        LinearLayout root = root();
        root.addView(header("Научное производство"));

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = content();

        body.addView(title("Научное производство"));

        body.addView(subtitle(
                "Центральная система производства научных продуктов института."
        ));

        addPrimaryButton(
                body,
                "📝 Научная статья",
                v -> createProduction("Научная статья")
        );

        addButton(
                body,
                "📚 Обзорная справка",
                v -> createProduction("Обзорная справка")
        );

        addButton(
                body,
                "🔬 Полная НИР",
                v -> createProduction("НИР")
        );

        addButton(
                body,
                "📄 Другой научный продукт",
                v -> createProduction("Другой научный продукт")
        );

        LinearLayout pipeline = card();

        pipeline.addView(cardTitle(
                "Производственный цикл"
        ));

        pipeline.addView(subtitle(
                "Техническое задание → декомпозиция → подбор агентов → " +
                        "источники → исследование → анализ документов → " +
                        "критик → рецензент → редактор → готовый продукт."
        ));

        body.addView(pipeline, marginParams());

        scroll.addView(body);

        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        setContentView(root);
    }

    private void createProduction(String productType) {

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), 0, dp(20), 0);

        EditText topic =
                edit("Тема");

        EditText pages =
                edit("Количество страниц");

        pages.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
        );

        EditText requirements =
                edit("Дополнительные требования");

        form.addView(topic);
        form.addView(pages);
        form.addView(requirements);

        new AlertDialog.Builder(this)
                .setTitle(productType)
                .setView(form)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Создать", (dialog, which) -> {

                    String t =
                            topic.getText().toString().trim();

                    if (t.isEmpty()) {
                        toast("Введите тему");
                        return;
                    }

                    String prompt =
                            "Подготовь структуру научного продукта.\n\n" +
                                    "Тип: " + productType + "\n" +
                                    "Тема: " + t + "\n" +
                                    "Объём: " +
                                    pages.getText().toString() +
                                    " страниц\n" +
                                    "Требования: " +
                                    requirements.getText().toString();

                    requestAI(
                            prompt,
                            productType
                    );
                })
                .show();
    }

    // ============================================================
    // AI НАСТРОЙКИ
    // ============================================================

    private void showAISettings() {

        LinearLayout root = root();
        root.addView(header("AI"));

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = content();

        body.addView(title("AI-система"));

        body.addView(subtitle(
                "Можно использовать облачные модели или локальный Qwen3 8B."
        ));

        String active =
                prefs.getString(
                        "active_provider",
                        "local"
                );

        for (AIProvider provider : providers) {

            LinearLayout c = card();

            c.addView(cardTitle(
                    provider.name
            ));

            c.addView(
                    subtitle(
                            "Модель: " +
                                    provider.defaultModel +
                                    "\n" +
                                    provider.baseUrl
                    )
            );

            Button select =
                    button(
                            provider.id.equals(active)
                                    ? "✓ Активный провайдер"
                                    : "Сделать активным"
                    );

            select.setOnClickListener(v -> {

                prefs.edit()
                        .putString(
                                "active_provider",
                                provider.id
                        )
                        .apply();

                toast(
                        "Активирован: " +
                                provider.name
                );

                showAISettings();
            });

            c.addView(select);

            if (!provider.local) {

                Button key =
                        button("Настроить API-ключ");

                key.setOnClickListener(v ->
                        configureApiKey(provider)
                );

                c.addView(key);
            }

            Button test =
                    button("Тестировать");

            test.setOnClickListener(v -> {

                if (provider.local) {
                    testLocalQwen();
                } else {
                    requestAI(
                            "Ответь одним предложением: что такое искусственный интеллект?",
                            "Тест " + provider.name
                    );
                }
            });

            c.addView(test);

            body.addView(c, marginParams());
        }

        scroll.addView(body);

        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        setContentView(root);
    }

    private void configureApiKey(AIProvider provider) {

        EditText input =
                edit("API-ключ");

        input.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        input.setText(
                prefs.getString(
                        "key_" + provider.id,
                        ""
                )
        );

        new AlertDialog.Builder(this)
                .setTitle(
                        "API: " +
                                provider.name
                )
                .setView(input)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить", (dialog, which) -> {

                    prefs.edit()
                            .putString(
                                    "key_" + provider.id,
                                    input.getText().toString()
                            )
                            .apply();

                    toast("Ключ сохранён");
                })
                .show();
    }

    // ============================================================
    // ЛОКАЛЬНЫЙ QWEN
    // ============================================================

    private void testLocalQwen() {

        String prompt =
                "Ответь одним коротким предложением: что такое искусственный интеллект?";

        requestAI(
                prompt,
                "Ответ локального Qwen"
        );
    }

    // ============================================================
    // ОБЩИЙ AI ЗАПРОС
    // ============================================================

    private void requestAI(
            String prompt,
            String dialogTitle) {

        String providerId =
                prefs.getString(
                        "active_provider",
                        "local"
                );

        AIProvider provider =
                findProvider(providerId);

        if (provider == null) {
            provider = providers.get(
                    providers.size() - 1
            );
        }

        final AIProvider selectedProvider =
                provider;

        ProgressDialog progress =
                new ProgressDialog(this);

        progress.setTitle(
                "Научный институт"
        );

        progress.setMessage(
                selectedProvider.local
                        ? "Локальный Qwen3 8B работает..."
                        : "AI выполняет запрос..."
        );

        progress.setCancelable(false);
        progress.show();

        final String finalPrompt = prompt;

        new Thread(() -> {

            try {

                String result;

                if (selectedProvider.id.equals("gemini")) {

                    result =
                            requestGemini(
                                    selectedProvider,
                                    finalPrompt
                            );

                } else {

                    result =
                            requestOpenAICompatible(
                                    selectedProvider,
                                    finalPrompt
                            );
                }

                String finalResult = result;

                runOnUiThread(() -> {

                    if (progress.isShowing()) {
                        progress.dismiss();
                    }

                    showAIResult(
                            dialogTitle,
                            finalResult
                    );
                });

            } catch (Exception e) {

                String error =
                        e.getClass().getSimpleName() +
                                ": " +
                                e.getMessage();

                runOnUiThread(() -> {

                    if (progress.isShowing()) {
                        progress.dismiss();
                    }

                    showAIResult(
                            dialogTitle,
                            "Ошибка AI:\n\n" +
                                    error
                    );
                });
            }

        }).start();
    }

    // ============================================================
    // OPENAI COMPATIBLE
    // ============================================================

    private String requestOpenAICompatible(
            AIProvider provider,
            String prompt) throws Exception {

        URL url =
                new URL(provider.baseUrl);

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

connection.setConnectTimeout(15000);
connection.setReadTimeout(300000);

        // --------------------------------------------------------
        // ВАЖНО:
        // локальный Qwen может генерировать долго.
        // Даём 15 секунд на подключение и 120 секунд на ответ.
        // --------------------------------------------------------

        connection.setConnectTimeout(15000);
        connection.setReadTimeout(120000);

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        if (!provider.local) {

            String key =
                    prefs.getString(
                            "key_" + provider.id,
                            ""
                    );

            if (key == null ||
                    key.trim().isEmpty()) {

                throw new Exception(
                        "API-ключ для " +
                                provider.name +
                                " не задан."
                );
            }

            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + key
            );
        }

        String model =
                prefs.getString(
                        "model_" + provider.id,
                        provider.defaultModel
                );

        JSONObject root =
                new JSONObject();

        root.put(
                "model",
                model
        );

        JSONArray messages =
                new JSONArray();

        JSONObject user =
                new JSONObject();

        user.put(
                "role",
                "user"
        );

        user.put(
                "content",
                prompt
        );

        messages.put(user);

        root.put(
                "messages",
                messages
        );

        // Небольшой первый тест.
        // После проверки работоспособности
        // можно увеличить значение.
        root.put(
                "temperature",
                0.2
        );

        root.put(
                "max_tokens",
                512
        );

        OutputStream os =
                connection.getOutputStream();

        os.write(
                root.toString()
                        .getBytes("UTF-8")
        );

        os.flush();
        os.close();

        int code =
                connection.getResponseCode();

        InputStream stream;

        if (code >= 200 &&
                code < 300) {

            stream =
                    connection.getInputStream();

        } else {

            stream =
                    connection.getErrorStream();

            if (stream == null) {
                throw new Exception(
                        "HTTP " + code
                );
            }
        }

        String response =
                readStream(stream);

        connection.disconnect();

        if (code < 200 ||
                code >= 300) {

            throw new Exception(
                    "HTTP " +
                            code +
                            "\n" +
                            response
            );
        }

        return parseCompatibleResponse(
                response
        );
    }

    // ============================================================
    // GEMINI
    // ============================================================

    private String requestGemini(
            AIProvider provider,
            String prompt) throws Exception {

        String key =
                prefs.getString(
                        "key_gemini",
                        ""
                );

        if (key == null ||
                key.trim().isEmpty()) {

            throw new Exception(
                    "API-ключ Gemini не задан."
            );
        }

        String model =
                prefs.getString(
                        "model_gemini",
                        provider.defaultModel
                );

        String urlString =
                provider.baseUrl +
                        model +
                        ":generateContent?key=" +
                        key;

        URL url =
                new URL(urlString);

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setConnectTimeout(15000);
        connection.setReadTimeout(120000);

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        connection.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        JSONObject root =
                new JSONObject();

        JSONArray contents =
                new JSONArray();

        JSONObject content =
                new JSONObject();

        content.put(
                "role",
                "user"
        );

        JSONArray parts =
                new JSONArray();

        JSONObject part =
                new JSONObject();

        part.put(
                "text",
                prompt
        );

        parts.put(part);

        content.put(
                "parts",
                parts
        );

        contents.put(content);

        root.put(
                "contents",
                contents
        );

        OutputStream os =
                connection.getOutputStream();

        os.write(
                root.toString()
                        .getBytes("UTF-8")
        );

        os.flush();
        os.close();

        int code =
                connection.getResponseCode();

        InputStream stream;

        if (code >= 200 &&
                code < 300) {

            stream =
                    connection.getInputStream();

        } else {

            stream =
                    connection.getErrorStream();

            if (stream == null) {
                throw new Exception(
                        "HTTP " + code
                );
            }
        }

        String response =
                readStream(stream);

        connection.disconnect();

        if (code < 200 ||
                code >= 300) {

            throw new Exception(
                    "HTTP " +
                            code +
                            "\n" +
                            response
            );
        }

        return parseGeminiResponse(
                response
        );
    }

    // ============================================================
    // JSON
    // ============================================================

    private String parseCompatibleResponse(
            String response) throws Exception {

        JSONObject root =
                new JSONObject(response);

        JSONArray choices =
                root.optJSONArray("choices");

        if (choices == null ||
                choices.length() == 0) {

            return response;
        }

        JSONObject choice =
                choices.getJSONObject(0);

        String finish =
                choice.optString(
                        "finish_reason",
                        ""
                );

        JSONObject message =
                choice.optJSONObject(
                        "message"
                );

        String content = "";

        if (message != null) {

            Object value =
                    message.opt("content");

            if (value != null &&
                    !JSONObject.NULL.equals(value)) {

                content =
                        String.valueOf(value);
            }
        }

        if (content.trim().isEmpty()) {
            content = response;
        }

        if ("length".equalsIgnoreCase(finish)) {

            content +=
                    "\n\n[Генерация остановлена по лимиту длины ответа.]";
        }

        return content;
    }

    private String parseGeminiResponse(
            String response) throws Exception {

        JSONObject root =
                new JSONObject(response);

        JSONArray candidates =
                root.optJSONArray("candidates");

        if (candidates == null ||
                candidates.length() == 0) {

            return response;
        }

        JSONObject candidate =
                candidates.getJSONObject(0);

        JSONObject content =
                candidate.optJSONObject(
                        "content"
                );

        if (content == null) {
            return response;
        }

        JSONArray parts =
                content.optJSONArray(
                        "parts"
                );

        if (parts == null ||
                parts.length() == 0) {

            return response;
        }

        StringBuilder result =
                new StringBuilder();

        for (int i = 0;
             i < parts.length();
             i++) {

            JSONObject part =
                    parts.optJSONObject(i);

            if (part != null) {

                String text =
                        part.optString(
                                "text",
                                ""
                        );

                if (!text.isEmpty()) {
                    result.append(text);
                }
            }
        }

        return result.length() == 0
                ? response
                : result.toString();
    }

    private String readStream(
            InputStream input)
            throws Exception {

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                input,
                                "UTF-8"
                        )
                );

        StringBuilder result =
                new StringBuilder();

        String line;

        while ((line =
                reader.readLine()) != null) {

            result.append(line);
        }

        reader.close();

        return result.toString();
    }

    // ============================================================
    // AI RESULT
    // ============================================================

    private void showAIResult(
            String title,
            String result) {

        TextView text =
                text(result, 16);

        text.setTextColor(TEXT);
        text.setPadding(
                dp(20),
                dp(5),
                dp(20),
                dp(5)
        );

        ScrollView scroll =
                new ScrollView(this);

        scroll.setPadding(
                dp(5),
                dp(5),
                dp(5),
                dp(5)
        );

        scroll.addView(text);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(scroll)
                .setPositiveButton(
                        "Понятно",
                        null
                )
                .show();
    }

    // ============================================================
    // УТИЛИТЫ
    // ============================================================

    private AIProvider findProvider(
            String id) {

        for (AIProvider p : providers) {

            if (p.id.equals(id)) {
                return p;
            }
        }

        return null;
    }

    private EditText edit(String hint) {

        EditText e =
                new EditText(this);

        e.setHint(hint);
        e.setTextSize(16);
        e.setSingleLine(false);
        e.setPadding(
                dp(12),
                dp(8),
                dp(12),
                dp(8)
        );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        p.setMargins(
                0,
                dp(4),
                0,
                dp(4)
        );

        e.setLayoutParams(p);

        return e;
    }

    private LinearLayout.LayoutParams marginParams() {

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        p.setMargins(
                0,
                dp(5),
                0,
                dp(5)
        );

        return p;
    }

    private void showInfo(
            String title,
            String message) {

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                        "Понятно",
                        null
                )
                .show();
    }

    private void toast(String message) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    // ============================================================
    // ЗАГРУЗКА ДАННЫХ
    // ============================================================

    private void loadData() {

        departments.clear();
        agents.clear();
        pipelines.clear();
        courses.clear();
        documents.clear();

        SQLiteDatabase database =
                db.getReadableDatabase();

        Cursor c =
                database.query(
                        "departments",
                        null,
                        null,
                        null,
                        null,
                        null,
                        "id ASC"
                );

        while (c.moveToNext()) {

            departments.add(
                    new Department(
                            c.getLong(
                                    c.getColumnIndexOrThrow("id")
                            ),
                            c.getString(
                                    c.getColumnIndexOrThrow("name")
                            ),
                            c.getString(
                                    c.getColumnIndexOrThrow("description")
                            )
                    )
            );
        }

        c.close();

        c =
                database.query(
                        "agents",
                        null,
                        null,
                        null,
                        null,
                        null,
                        "id ASC"
                );

        while (c.moveToNext()) {

            agents.add(
                    new Agent(
                            c.getLong(
                                    c.getColumnIndexOrThrow("id")
                            ),
                            c.getString(
                                    c.getColumnIndexOrThrow("name")
                            ),
                            c.getString(
                                    c.getColumnIndexOrThrow("role")
                            ),
                            c.getString(
                                    c.getColumnIndexOrThrow("department")
                            ),
                            c.getString(
                                    c.getColumnIndexOrThrow("qualification")
                            ),
                            c.getString(
                                    c.getColumnIndexOrThrow("competencies")
                            )
                    )
            );
        }

        c.close();

        c =
                database.query(
                        "pipelines",
                        null,
                        null,
                        null,
                        null,
                        null,
                        "id DESC"
                );

        while (c.moveToNext()) {

            pipelines.add(
                    new Pipeline(
                            c.getLong(
                                    c.getColumnIndexOrThrow("id")
                            ),
                            c.getString(
                                    c.getColumnIndexOrThrow("title")
                            ),
                            c.getString(
                                    c.getColumnIndexOrThrow("type")
                            ),
                            c.getString(
                                    c.getColumnIndexOrThrow("pages")
                            ),
                            c.getString(
                                    c.getColumnIndexOrThrow("standard")
                            ),
                            c.getString(
                                    c.getColumnIndexOrThrow("deadline")
                            ),
                            c.getString(
                                    c.getColumnIndexOrThrow("field")
                            ),
                            c.getString(
                                    c.getColumnIndexOrThrow("web")
                            ),
                            c.getString(
                                    c.getColumnIndexOrThrow("review")
                            ),
                            c.getString(
                                    c.getColumnIndexOrThrow("status")
                            )
                    )
            );
        }

        c.close();

        c =
                database.query(
                        "courses",
                        null,
                        null,
                        null,
                        null,
                        null,
                        "id DESC"
                );

        while (c.moveToNext()) {

            courses.add(
                    new Course(
                            c.getLong(
                                    c.getColumnIndexOrThrow("id")
                            ),
                            c.getString(
                                    c.getColumnIndexOrThrow("title")
                            ),
                            c.getString(
                                    c.getColumnIndexOrThrow("material")
                            ),
                            c.getString(
                                    c.getColumnIndexOrThrow("status")
                            )
                    )
            );
        }

        c.close();

        c =
                database.query(
                        "documents",
                        null,
                        null,
                        null,
                        null,
                        null,
                        "id DESC"
                );

        while (c.moveToNext()) {

            documents.add(
                    new DocumentItem(
                            c.getLong(
                                    c.getColumnIndexOrThrow("id")
                            ),
                            c.getString(
                                    c.getColumnIndexOrThrow("name")
                            ),
                            c.getString(
                                    c.getColumnIndexOrThrow("uri")
                            )
                    )
            );
        }

        c.close();
    }

    // ============================================================
    // SEED
    // ============================================================

    private void seedIfNeeded() {

        if (!departments.isEmpty()) {
            return;
        }

        long d1 =
                db.dep(
                        "Кафедра искусственного интеллекта",
                        "Интеллектуальные системы и машинное обучение"
                );

        long d2 =
                db.dep(
                        "Кафедра информационной безопасности",
                        "Защита информации и цифровая криминалистика"
                );

        long d3 =
                db.dep(
                        "Кафедра радиотехники",
                        "Радиосистемы, электроника и обработка сигналов"
                );

        departments.add(
                new Department(
                        d1,
                        "Кафедра искусственного интеллекта",
                        "Интеллектуальные системы и машинное обучение"
                )
        );

        departments.add(
                new Department(
                        d2,
                        "Кафедра информационной безопасности",
                        "Защита информации и цифровая криминалистика"
                )
        );

        departments.add(
                new Department(
                        d3,
                        "Кафедра радиотехники",
                        "Радиосистемы, электроника и обработка сигналов"
                )
        );

        long a1 =
                db.agent(
                        "Александр Ньютон",
                        "Научный исследователь",
                        "Кафедра искусственного интеллекта",
                        "Исследователь",
                        "ИИ; машинное обучение; анализ данных"
                );

        long a2 =
                db.agent(
                        "София Ковалевская",
                        "Методолог",
                        "Кафедра искусственного интеллекта",
                        "Методолог",
                        "математическое моделирование; методология исследований"
                );

        long a3 =
                db.agent(
                        "Иван Попов",
                        "Эксперт-рецензент",
                        "Кафедра информационной безопасности",
                        "Эксперт",
                        "кибербезопасность; рецензирование"
                );

        long a4 =
                db.agent(
                        "Мария Соколова",
                        "Научный редактор",
                        "Кафедра радиотехники",
                        "Редактор",
                        "научное редактирование; публикационная подготовка"
                );

        agents.add(
                new Agent(
                        a1,
                        "Александр Ньютон",
                        "Научный исследователь",
                        "Кафедра искусственного интеллекта",
                        "Исследователь",
                        "ИИ; машинное обучение; анализ данных"
                )
        );

        agents.add(
                new Agent(
                        a2,
                        "София Ковалевская",
                        "Методолог",
                        "Кафедра искусственного интеллекта",
                        "Методолог",
                        "математическое моделирование; методология исследований"
                )
        );

        agents.add(
                new Agent(
                        a3,
                        "Иван Попов",
                        "Эксперт-рецензент",
                        "Кафедра информационной безопасности",
                        "Эксперт",
                        "кибербезопасность; рецензирование"
                )
        );

        agents.add(
                new Agent(
                        a4,
                        "Мария Соколова",
                        "Научный редактор",
                        "Кафедра радиотехники",
                        "Редактор",
                        "научное редактирование; публикационная подготовка"
                )
        );
    }

    // ============================================================
    // SQLITE
    // ============================================================

    static class DB extends SQLiteOpenHelper {

        private static final String NAME =
                "institute.db";

        private static final int VERSION =
                2;

        DB(Context context) {
            super(
                    context,
                    NAME,
                    null,
                    VERSION
            );
        }

        @Override
        public void onCreate(
                SQLiteDatabase db) {

            db.execSQL(
                    "CREATE TABLE departments (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "name TEXT NOT NULL," +
                            "description TEXT)"
            );

            db.execSQL(
                    "CREATE TABLE agents (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "name TEXT NOT NULL," +
                            "role TEXT," +
                            "department TEXT," +
                            "qualification TEXT," +
                            "competencies TEXT)"
            );

            db.execSQL(
                    "CREATE TABLE pipelines (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "title TEXT NOT NULL," +
                            "type TEXT," +
                            "pages TEXT," +
                            "standard TEXT," +
                            "deadline TEXT," +
                            "field TEXT," +
                            "web TEXT," +
                            "review TEXT," +
                            "status TEXT)"
            );

            db.execSQL(
                    "CREATE TABLE courses (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "title TEXT NOT NULL," +
                            "material TEXT," +
                            "status TEXT)"
            );

            db.execSQL(
                    "CREATE TABLE documents (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "name TEXT," +
                            "uri TEXT)"
            );
        }

        @Override
        public void onUpgrade(
                SQLiteDatabase db,
                int oldVersion,
                int newVersion) {

            if (oldVersion < 2) {

                try {
                    db.execSQL(
                            "ALTER TABLE agents ADD COLUMN qualification TEXT"
                    );
                } catch (Exception ignored) {
                }

                try {
                    db.execSQL(
                            "ALTER TABLE agents ADD COLUMN competencies TEXT"
                    );
                } catch (Exception ignored) {
                }
            }
        }

        long dep(
                String name,
                String description) {

            ContentValues v =
                    new ContentValues();

            v.put(
                    "name",
                    name
            );

            v.put(
                    "description",
                    description
            );

            return getWritableDatabase()
                    .insert(
                            "departments",
                            null,
                            v
                    );
        }

        long agent(
                String name,
                String role,
                String department,
                String qualification,
                String competencies) {

            ContentValues v =
                    new ContentValues();

            v.put(
                    "name",
                    name
            );

            v.put(
                    "role",
                    role
            );

            v.put(
                    "department",
                    department
            );

            v.put(
                    "qualification",
                    qualification
            );

            v.put(
                    "competencies",
                    competencies
            );

            return getWritableDatabase()
                    .insert(
                            "agents",
                            null,
                            v
                    );
        }

        void updateAgent(Agent a) {

            ContentValues v =
                    new ContentValues();

            v.put(
                    "qualification",
                    a.qualification
            );

            v.put(
                    "competencies",
                    a.competencies
            );

            getWritableDatabase()
                    .update(
                            "agents",
                            v,
                            "id=?",
                            new String[]{
                                    String.valueOf(a.id)
                            }
                    );
        }

        long pipeline(Pipeline p) {

            ContentValues v =
                    new ContentValues();

            v.put(
                    "title",
                    p.title
            );

            v.put(
                    "type",
                    p.type
            );

            v.put(
                    "pages",
                    p.pages
            );

            v.put(
                    "standard",
                    p.standard
            );

            v.put(
                    "deadline",
                    p.deadline
            );

            v.put(
                    "field",
                    p.field
            );

            v.put(
                    "web",
                    p.web
            );

            v.put(
                    "review",
                    p.review
            );

            v.put(
                    "status",
                    p.status
            );

            return getWritableDatabase()
                    .insert(
                            "pipelines",
                            null,
                            v
                    );
        }

        long course(
                String title,
                String material,
                String status) {

            ContentValues v =
                    new ContentValues();

            v.put(
                    "title",
                    title
            );

            v.put(
                    "material",
                    material
            );

            v.put(
                    "status",
                    status
            );

            return getWritableDatabase()
                    .insert(
                            "courses",
                            null,
                            v
                    );
        }

        long doc(
                String name,
                String uri) {

            ContentValues v =
                    new ContentValues();

            v.put(
                    "name",
                    name
            );

            v.put(
                    "uri",
                    uri
            );

            return getWritableDatabase()
                    .insert(
                            "documents",
                            null,
                            v
                    );
        }
    }
}