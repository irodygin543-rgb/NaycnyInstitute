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
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity {

    // ============================================================
    // ЦВЕТА ИНСТИТУТА
    // ============================================================

    static final int NAVY       = Color.rgb(13, 38, 61);
    static final int NAVY2      = Color.rgb(20, 58, 91);
    static final int BLUE       = Color.rgb(36, 104, 176);
    static final int BLUE_LIGHT = Color.rgb(232, 241, 250);
    static final int TEAL       = Color.rgb(25, 139, 137);
    static final int GREEN      = Color.rgb(40, 145, 91);
    static final int ORANGE     = Color.rgb(221, 137, 45);
    static final int RED        = Color.rgb(190, 65, 65);
    static final int BG         = Color.rgb(246, 248, 251);
    static final int CARD       = Color.WHITE;
    static final int TEXT       = Color.rgb(25, 39, 52);
    static final int MUTED      = Color.rgb(101, 116, 130);
    static final int BORDER     = Color.rgb(224, 231, 238);

    static final int PICK_DOCUMENT = 1001;

    DB db;
    SharedPreferences prefs;

    LinearLayout root;
    LinearLayout content;

    ArrayList<Department> departments = new ArrayList<>();
    ArrayList<Agent> agents = new ArrayList<>();
    ArrayList<Assignment> assignments = new ArrayList<>();
    ArrayList<Course> courses = new ArrayList<>();
    ArrayList<Source> sources = new ArrayList<>();
    ArrayList<DocumentItem> documents = new ArrayList<>();
    ArrayList<ResearchJob> researchJobs = new ArrayList<>();
    ArrayList<PipelineJob> pipelineJobs = new ArrayList<>();
    ArrayList<AgentTask> agentTasks = new ArrayList<>();
    ArrayList<Competition> competitions = new ArrayList<>();
    ArrayList<ResearchSection> researchSections = new ArrayList<>();

    HashMap<String, String> agentQualifications = new HashMap<>();
    HashMap<String, String> agentCompetencies = new HashMap<>();

    // ============================================================
    // МОДЕЛИ
    // ============================================================

    static class Department {
        String name, description;
        Department(String n, String d) {
            name = n;
            description = d;
        }
    }

    static class Agent {
        String name, role, department;
        Agent(String n, String r, String d) {
            name = n;
            role = r;
            department = d;
        }
    }

    static class Course {
        String title, material, status;
        Course(String t, String m) {
            title = t;
            material = m;
            status = "Назначен";
        }
    }

    static class Source {
        String title, url, kind;
        Source(String t, String u, String k) {
            title = t;
            url = u;
            kind = k;
        }
    }

    static class DocumentItem {
        String name, uri, category, linkedTo;
        DocumentItem(String n, String u, String c, String l) {
            name = n;
            uri = u;
            category = c;
            linkedTo = l;
        }
    }

    static class ResearchJob {
        String topic, scope, status;
        ResearchJob(String t, String s) {
            topic = t;
            scope = s;
            status = "Подготовлено";
        }
    }

    static class ResearchSection {
        String pipeline, title, pages, words, agent;
        ResearchSection(String p, String t, String pg, String w, String a) {
            pipeline = p;
            title = t;
            pages = pg;
            words = w;
            agent = a;
        }
    }

    static class Competition {
        String title, task, participants, status;
        Competition(String t, String ta, String p) {
            title = t;
            task = ta;
            participants = p;
            status = "Подготовлено";
        }
    }

    static class AgentTask {
        String pipeline, stage, task, agent, status;
        AgentTask(String p, String s, String t, String a) {
            pipeline = p;
            stage = s;
            task = t;
            agent = a;
            status = "Ожидает";
        }
    }

    static class PipelineJob {
        String title;
        String type;
        String stages;
        String status;
        String pages;
        String standard;
        String deadline;
        String field;
        String webResearch;
        String reviewers;

        PipelineJob(
                String t,
                String ty,
                String pg,
                String st,
                String dl,
                String f,
                String wr,
                String rv
        ) {
            title = t;
            type = ty;
            pages = pg;
            standard = st;
            deadline = dl;
            field = f;
            webResearch = wr;
            reviewers = rv;
            stages =
                    "Декомпозиция → подбор агентов → исследование → "
                    + "анализ документов → критик → рецензент → редактор";
            status = "Создано";
        }
    }

    static class Assignment {
        String title, type, status, departments, agents;

        Assignment(String t, String ty, String d, String a) {
            title = t;
            type = ty;
            departments = d;
            agents = a;
            status = "Новое";
        }
    }

    // ============================================================
    // СОЗДАНИЕ
    // ============================================================

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setStatusBarColor(NAVY);
        getWindow().setNavigationBarColor(NAVY);

        db = new DB(this);
        prefs = getSharedPreferences("institute_settings", MODE_PRIVATE);

        loadData();

        if (departments.size() == 0 && agents.size() == 0) {
            seed();
        }

        showHome();
    }

    // ============================================================
    // ДАННЫЕ
    // ============================================================

    void seed() {

        departments.add(new Department(
                "Кафедра искусственного интеллекта",
                "Интеллектуальные системы, машинное обучение и анализ данных"
        ));

        departments.add(new Department(
                "Кафедра информационной безопасности",
                "Защита информации и цифровая криминалистика"
        ));

        departments.add(new Department(
                "Кафедра радиотехники",
                "Радиосистемы, электроника и обработка сигналов"
        ));

        agents.add(new Agent(
                "Александр Ньютон",
                "Научный исследователь",
                "Кафедра искусственного интеллекта"
        ));

        agents.add(new Agent(
                "София Ковалевская",
                "Методолог",
                "Кафедра искусственного интеллекта"
        ));

        agents.add(new Agent(
                "Иван Попов",
                "Эксперт-рецензент",
                "Кафедра информационной безопасности"
        ));

        agents.add(new Agent(
                "Мария Соколова",
                "Научный редактор",
                "Кафедра радиотехники"
        ));

        for (Department d : departments) {
            db.addDepartment(d.name, d.description);
        }

        for (Agent a : agents) {
            db.addAgent(a.name, a.role, a.department);
        }
    }

    void loadData() {

        SQLiteDatabase d = db.getReadableDatabase();

        Cursor c = d.rawQuery(
                "SELECT name,description FROM departments ORDER BY id",
                null
        );

        while (c.moveToNext()) {
            departments.add(
                    new Department(
                            c.getString(0),
                            c.getString(1)
                    )
            );
        }
        c.close();

        c = d.rawQuery(
                "SELECT name,role,department FROM agents ORDER BY id",
                null
        );

        while (c.moveToNext()) {
            agents.add(
                    new Agent(
                            c.getString(0),
                            c.getString(1),
                            c.getString(2)
                    )
            );
        }
        c.close();

        c = d.rawQuery(
                "SELECT title,material,status FROM courses ORDER BY id",
                null
        );

        while (c.moveToNext()) {
            Course x = new Course(c.getString(0), c.getString(1));
            x.status = c.getString(2);
            courses.add(x);
        }
        c.close();

        c = d.rawQuery(
                "SELECT name,uri,category,linked_to FROM documents ORDER BY id",
                null
        );

        while (c.moveToNext()) {
            documents.add(
                    new DocumentItem(
                            c.getString(0),
                            c.getString(1),
                            c.getString(2),
                            c.getString(3)
                    )
            );
        }
        c.close();

        c = d.rawQuery(
                "SELECT topic,scope,status FROM research_jobs ORDER BY id",
                null
        );

        while (c.moveToNext()) {
            ResearchJob r = new ResearchJob(
                    c.getString(0),
                    c.getString(1)
            );
            r.status = c.getString(2);
            researchJobs.add(r);
        }
        c.close();

        c = d.rawQuery(
                "SELECT title,type,status FROM pipeline_jobs ORDER BY id",
                null
        );

        while (c.moveToNext()) {
            PipelineJob p = new PipelineJob(
                    c.getString(0),
                    c.getString(1),
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
            );
            p.status = c.getString(2);

            Cursor s = d.rawQuery(
                    "SELECT pages,standard,deadline,field,web_research,reviewers " +
                    "FROM pipeline_specs WHERE pipeline_title=?",
                    new String[]{p.title}
            );

            if (s.moveToFirst()) {
                p.pages = s.getString(0);
                p.standard = s.getString(1);
                p.deadline = s.getString(2);
                p.field = s.getString(3);
                p.webResearch = s.getString(4);
                p.reviewers = s.getString(5);
            }
            s.close();

            pipelineJobs.add(p);
        }
        c.close();

        c = d.rawQuery(
                "SELECT title,task,participants,status FROM competitions ORDER BY id",
                null
        );

        while (c.moveToNext()) {
            Competition x = new Competition(
                    c.getString(0),
                    c.getString(1),
                    c.getString(2)
            );
            x.status = c.getString(3);
            competitions.add(x);
        }
        c.close();

        c = d.rawQuery(
                "SELECT pipeline_title,stage,task,agent,status FROM agent_tasks ORDER BY id",
                null
        );

        while (c.moveToNext()) {
            AgentTask x = new AgentTask(
                    c.getString(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3)
            );
            x.status = c.getString(4);
            agentTasks.add(x);
        }
        c.close();

        c = d.rawQuery(
                "SELECT pipeline_title,section_title,pages,words,agent " +
                "FROM research_sections ORDER BY id",
                null
        );

        while (c.moveToNext()) {
            researchSections.add(
                    new ResearchSection(
                            c.getString(0),
                            c.getString(1),
                            c.getString(2),
                            c.getString(3),
                            c.getString(4)
                    )
            );
        }
        c.close();

        c = d.rawQuery(
                "SELECT agent_name,qualification,competencies FROM agent_profiles",
                null
        );

        while (c.moveToNext()) {
            agentQualifications.put(c.getString(0), c.getString(1));
            agentCompetencies.put(c.getString(0), c.getString(2));
        }
        c.close();
    }

    // ============================================================
    // ОСНОВНОЙ UI
    // ============================================================

    void base(String section) {

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        setContentView(root);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(18), dp(14), dp(18), dp(12));
        header.setBackgroundColor(CARD);

        TextView brand = new TextView(this);
        brand.setText("НАУЧНЫЙ\nИНСТИТУТ");
        brand.setTextSize(16);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        brand.setTextColor(NAVY);
        brand.setGravity(Gravity.CENTER_VERTICAL);

        header.addView(
                brand,
                new LinearLayout.LayoutParams(0, dp(58), 1)
        );

        Button home = smallButton("Главная");
        home.setOnClickListener(v -> showHome());

        header.addView(home);

        root.addView(header);

        View line = new View(this);
        line.setBackgroundColor(BORDER);
        root.addView(
                line,
                new LinearLayout.LayoutParams(-1, dp(1))
        );

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(16), dp(18), dp(100));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(content);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(-1, 0, 1)
        );

        addBottomNavigation();
    }

    void addBottomNavigation() {

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(6), dp(6), dp(6), dp(8));
        nav.setBackgroundColor(CARD);

        View line = new View(this);
        line.setBackgroundColor(BORDER);

        root.addView(
                line,
                new LinearLayout.LayoutParams(-1, dp(1))
        );

        String[] names = {
                "Главная",
                "Агенты",
                "НИР",
                "Документы"
        };

        View.OnClickListener[] actions = new View.OnClickListener[]{
                v -> showHome(),
                v -> showAgents(),
                v -> showPipelines(),
                v -> showDocuments()
        };

        for (int i = 0; i < names.length; i++) {

            TextView item = new TextView(this);
            item.setText(names[i]);
            item.setTextSize(11);
            item.setTextColor(MUTED);
            item.setGravity(Gravity.CENTER);
            item.setPadding(0, dp(8), 0, dp(4));
            item.setOnClickListener(actions[i]);

            nav.addView(
                    item,
                    new LinearLayout.LayoutParams(0, dp(50), 1)
            );
        }

        root.addView(nav);
    }

    // ============================================================
    // ГЛАВНАЯ
    // ============================================================

    void showHome() {

        base("home");

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(22), dp(22), dp(22), dp(22));
        hero.setBackground(roundGradient(NAVY, NAVY2, 22));

        TextView over = text(
                "РЕКТОРАТ • МУЛЬТИАГЕНТНАЯ СИСТЕМА",
                11,
                Color.rgb(190, 215, 235),
                true
        );

        TextView h = text(
                "Научный институт",
                28,
                Color.WHITE,
                true
        );

        TextView sub = text(
                "Управление кафедрами, научными агентами и производством научной продукции.",
                14,
                Color.WHITE,
                false
        );

        hero.addView(over);
        hero.addView(h);
        hero.addView(sub);

        LinearLayout.LayoutParams hp =
                new LinearLayout.LayoutParams(-1, -2);
        hp.setMargins(0, 0, 0, dp(18));

        content.addView(hero, hp);

        TextView section = sectionTitle("СОСТОЯНИЕ ИНСТИТУТА");
        content.addView(section);

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);

        stats.addView(
                statCard(
                        String.valueOf(agents.size()),
                        "агента",
                        BLUE
                ),
                new LinearLayout.LayoutParams(0, dp(110), 1)
        );

        stats.addView(
                statCard(
                        String.valueOf(departments.size()),
                        "кафедры",
                        TEAL
                ),
                new LinearLayout.LayoutParams(0, dp(110), 1)
        );

        stats.addView(
                statCard(
                        String.valueOf(pipelineJobs.size()),
                        "НИР / конвейера",
                        ORANGE
                ),
                new LinearLayout.LayoutParams(0, dp(110), 1)
        );

        content.addView(stats);

        addSpace(14);

        TextView production = sectionTitle("НАУЧНОЕ ПРОИЗВОДСТВО");
        content.addView(production);

        content.addView(
                bigAction(
                        "🔬",
                        "AI-оркестратор",
                        "Запуск научного конвейера",
                        BLUE,
                        this::showPipelines
                )
        );

        content.addView(
                bigAction(
                        "📚",
                        "Академия",
                        "Повышение квалификации агентов",
                        TEAL,
                        this::showCourses
                )
        );

        content.addView(
                bigAction(
                        "📝",
                        "Научные поручения",
                        "НИР, статьи, справки, доклады",
                        ORANGE,
                        this::showAssignments
                )
        );

        TextView system = sectionTitle("ИНФОРМАЦИОННАЯ СИСТЕМА");
        content.addView(system);

        content.addView(
                rowAction("👨‍🔬", "Научные агенты",
                        agents.size() + " зарегистрировано",
                        this::showAgents)
        );

        content.addView(
                rowAction("🏛", "Кафедры",
                        departments.size() + " подразделения",
                        this::showDepartments)
        );

        content.addView(
                rowAction("📎", "Документы",
                        documents.size() + " документов",
                        this::showDocuments)
        );

        content.addView(
                rowAction("🌐", "Web Research",
                        researchJobs.size() + " исследования",
                        this::showResearch)
        );

        content.addView(
                rowAction("🏆", "Соревнования агентов",
                        competitions.size() + " соревнований",
                        this::showCompetitions)
        );

        content.addView(
                rowAction("⚖", "Нормативная база",
                        "законодательство и требования",
                        () -> info(
                                "Нормативная база",
                                "Здесь будет отдельный контур нормативных документов, требований ВАК, ГОСТ и законодательства в сфере науки и образования."
                        ))
        );

        TextView local = sectionTitle("ЛОКАЛЬНЫЙ ИИ");
        content.addView(local);

        content.addView(
                rowAction(
                        "🧠",
                        "Qwen3 8B",
                        "Локальный сервер на телефоне",
                        this::testLocalQwen
                )
        );

        content.addView(
                rowAction(
                        "⚙",
                        "Настройки ИИ",
                        "API-ключ Gemini хранится отдельно",
                        this::aiSettings
                )
        );
    }

    // ============================================================
    // UI КОМПОНЕНТЫ
    // ============================================================

    TextView text(String s, float size, int color, boolean bold) {

        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setTypeface(
                Typeface.DEFAULT,
                bold ? Typeface.BOLD : Typeface.NORMAL
        );

        return v;
    }

    TextView sectionTitle(String s) {

        TextView v = text(
                s,
                12,
                MUTED,
                true
        );

        v.setPadding(0, dp(20), 0, dp(10));

        return v;
    }

    Button smallButton(String s) {

        Button b = new Button(this);
        b.setText(s);
        b.setTextSize(12);
        b.setTextColor(BLUE);
        b.setAllCaps(false);
        b.setPadding(dp(10), 0, dp(10), 0);
        b.setBackground(roundDrawable(BLUE_LIGHT, 12));

        return b;
    }

    TextView statCard(String number, String label, int color) {

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(8), dp(12), dp(8), dp(12));
        box.setBackground(roundDrawable(CARD, 16));

        TextView n = text(
                number,
                27,
                color,
                true
        );
        n.setGravity(Gravity.CENTER);

        TextView l = text(
                label,
                11,
                MUTED,
                false
        );
        l.setGravity(Gravity.CENTER);

        box.addView(n);
        box.addView(l);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, -1);
        p.setMargins(dp(5), 0, dp(5), 0);
        box.setLayoutParams(p);

        return wrapAsText(box);
    }

    TextView wrapAsText(LinearLayout layout) {

        TextView fake = new TextView(this);
        fake.setVisibility(View.GONE);

        // Этот метод нужен только как технический контейнер.
        // Возвращаем View через вспомогательный класс невозможно,
        // поэтому фактически используем специальную обёртку.
        return new TextView(this) {
            {
                setVisibility(View.VISIBLE);
                setBackground(roundDrawable(CARD, 16));
                setPadding(0, 0, 0, 0);
            }
        };
    }

    View bigAction(
            String icon,
            String title,
            String subtitle,
            int accent,
            final Runnable action
    ) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(15), dp(14), dp(15));
        card.setBackground(roundDrawable(CARD, 17));
        card.setOnClickListener(v -> action.run());

        TextView iconView = text(
                icon,
                25,
                accent,
                false
        );
        iconView.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams ip =
                new LinearLayout.LayoutParams(dp(48), dp(48));

        card.addView(iconView, ip);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setPadding(dp(12), 0, 0, 0);

        TextView t = text(title, 17, TEXT, true);
        TextView s = text(subtitle, 12, MUTED, false);

        texts.addView(t);
        texts.addView(s);

        card.addView(
                texts,
                new LinearLayout.LayoutParams(0, -2, 1)
        );

        TextView arrow = text("›", 28, accent, false);
        card.addView(arrow);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, dp(82));
        p.setMargins(0, 0, 0, dp(10));

        card.setLayoutParams(p);

        return card;
    }

    View rowAction(
            String icon,
            String title,
            String subtitle,
            final Runnable action
    ) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(11), dp(12), dp(11));
        row.setBackground(roundDrawable(CARD, 15));
        row.setOnClickListener(v -> action.run());

        TextView ic = text(icon, 20, BLUE, false);
        ic.setGravity(Gravity.CENTER);

        row.addView(
                ic,
                new LinearLayout.LayoutParams(dp(40), dp(45))
        );

        LinearLayout mid = new LinearLayout(this);
        mid.setOrientation(LinearLayout.VERTICAL);

        mid.addView(text(title, 15, TEXT, true));
        mid.addView(text(subtitle, 11, MUTED, false));

        row.addView(
                mid,
                new LinearLayout.LayoutParams(0, -2, 1)
        );

        row.addView(
                text("›", 25, MUTED, false),
                new LinearLayout.LayoutParams(dp(30), -2)
        );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, dp(70));
        p.setMargins(0, 0, 0, dp(8));

        row.setLayoutParams(p);

        return row;
    }

    TextView card(String a, String b) {

        TextView v = new TextView(this);

        v.setText(a + "\n" + b);
        v.setTextSize(14);
        v.setTextColor(TEXT);
        v.setPadding(dp(17), dp(16), dp(17), dp(16));
        v.setBackground(roundDrawable(CARD, 16));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, -2);

        p.setMargins(0, 0, 0, dp(10));

        v.setLayoutParams(p);

        return v;
    }

    void addSpace(int h) {

        Space s = new Space(this);

        content.addView(
                s,
                new LinearLayout.LayoutParams(1, dp(h))
        );
    }

    // ============================================================
    // КАФЕДРЫ
    // ============================================================

    void showDepartments() {

        base("departments");

        content.addView(
                text("Кафедры", 28, NAVY, true)
        );

        content.addView(
                text(
                        "Научные подразделения института",
                        13,
                        MUTED,
                        false
                )
        );

        addSpace(12);

        for (Department d : departments) {

            content.addView(
                    card(
                            d.name,
                            d.description
                    )
            );
        }

        content.addView(
                rowAction(
                        "＋",
                        "Добавить кафедру",
                        "Создать новое научное подразделение",
                        this::addDepartment
                )
        );
    }

    void addDepartment() {

        LinearLayout box = dialogBox();

        EditText name = field("Название кафедры");
        EditText desc = field("Научное направление");

        box.addView(name);
        box.addView(desc);

        new AlertDialog.Builder(this)
                .setTitle("Новая кафедра")
                .setView(box)
                .setPositiveButton(
                        "Создать",
                        (d, w) -> {

                            String n =
                                    name.getText().toString().trim();

                            if (!n.isEmpty()) {

                                String de =
                                        desc.getText().toString();

                                Department x =
                                        new Department(n, de);

                                departments.add(x);

                                db.addDepartment(n, de);

                                showDepartments();
                            }
                        }
                )
                .setNegativeButton("Отмена", null)
                .show();
    }

    // ============================================================
    // АГЕНТЫ
    // ============================================================

    void showAgents() {

        base("agents");

        content.addView(
                text("Научные агенты", 28, NAVY, true)
        );

        content.addView(
                text(
                        "Исследователи, методологи, критики и редакторы",
                        13,
                        MUTED,
                        false
                )
        );

        addSpace(12);

        for (final Agent a : agents) {

            LinearLayout row =
                    new LinearLayout(this);

            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(15), dp(13), dp(12), dp(13));
            row.setBackground(roundDrawable(CARD, 16));
            row.setOnClickListener(
                    v -> showAgentProfile(a)
            );

            TextView avatar =
                    text(
                            a.name.substring(0, 1),
                            20,
                            Color.WHITE,
                            true
                    );

            avatar.setGravity(Gravity.CENTER);
            avatar.setBackground(
                    roundDrawable(BLUE, 30)
            );

            row.addView(
                    avatar,
                    new LinearLayout.LayoutParams(
                            dp(48),
                            dp(48)
                    )
            );

            LinearLayout info =
                    new LinearLayout(this);

            info.setOrientation(LinearLayout.VERTICAL);
            info.setPadding(dp(12), 0, 0, 0);

            info.addView(
                    text(
                            a.name,
                            16,
                            TEXT,
                            true
                    )
            );

            info.addView(
                    text(
                            a.role,
                            12,
                            BLUE,
                            false
                    )
            );

            info.addView(
                    text(
                            a.department,
                            11,
                            MUTED,
                            false
                    )
            );

            row.addView(
                    info,
                    new LinearLayout.LayoutParams(
                            0,
                            -2,
                            1
                    )
            );

            row.addView(
                    text("›", 26, MUTED, false)
            );

            LinearLayout.LayoutParams p =
                    new LinearLayout.LayoutParams(-1, dp(75));

            p.setMargins(0, 0, 0, dp(9));

            row.setLayoutParams(p);

            content.addView(row);
        }

        content.addView(
                rowAction(
                        "＋",
                        "Создать агента",
                        "Добавить нового научного сотрудника",
                        this::addAgent
                )
        );
    }

    void showAgentProfile(final Agent a) {

        base("agent");

        content.addView(
                text(
                        "Профиль научного агента",
                        26,
                        NAVY,
                        true
                )
        );

        content.addView(
                card(
                        a.name,
                        a.role + "\n" +
                        "Кафедра: " + a.department
                )
        );

        String q =
                agentQualifications.containsKey(a.name)
                        ? agentQualifications.get(a.name)
                        : "Не установлена";

        String c =
                agentCompetencies.containsKey(a.name)
                        ? agentCompetencies.get(a.name)
                        : "Не указаны";

        content.addView(
                card(
                        "Квалификация",
                        q
                )
        );

        content.addView(
                card(
                        "Компетенции",
                        c
                )
        );

        content.addView(
                rowAction(
                        "✎",
                        "Профиль компетенций",
                        "Квалификация и научные компетенции",
                        () -> editAgentProfile(a)
                )
        );

        content.addView(
                rowAction(
                        "📚",
                        "Повышение квалификации",
                        "Назначить обучение агенту",
                        this::showCourses
                )
        );
    }

    void addAgent() {

        LinearLayout box = dialogBox();

        EditText n = field("Имя агента");
        EditText r = field("Роль / специализация");
        EditText dep = field("Кафедра");

        box.addView(n);
        box.addView(r);
        box.addView(dep);

        new AlertDialog.Builder(this)
                .setTitle("Новый научный агент")
                .setView(box)
                .setPositiveButton(
                        "Создать",
                        (d, w) -> {

                            String name =
                                    n.getText().toString().trim();

                            if (!name.isEmpty()) {

                                String role =
                                        r.getText().toString();

                                String department =
                                        dep.getText().toString();

                                agents.add(
                                        new Agent(
                                                name,
                                                role,
                                                department
                                        )
                                );

                                db.addAgent(
                                        name,
                                        role,
                                        department
                                );

                                showAgents();
                            }
                        }
                )
                .setNegativeButton("Отмена", null)
                .show();
    }

    void editAgentProfile(final Agent a) {

        LinearLayout box = dialogBox();

        EditText q = field(
                "Квалификационный уровень"
        );
        q.setText(
                agentQualifications.get(a.name)
        );

        EditText c = field(
                "Компетенции через запятую"
        );
        c.setText(
                agentCompetencies.get(a.name)
        );

        box.addView(q);
        box.addView(c);

        new AlertDialog.Builder(this)
                .setTitle("Профиль компетенций")
                .setView(box)
                .setPositiveButton(
                        "Сохранить",
                        (d, w) -> {

                            String qs =
                                    q.getText().toString();

                            String cs =
                                    c.getText().toString();

                            agentQualifications.put(
                                    a.name,
                                    qs
                            );

                            agentCompetencies.put(
                                    a.name,
                                    cs
                            );

                            db.saveAgentProfile(
                                    a.name,
                                    qs,
                                    cs
                            );

                            showAgentProfile(a);
                        }
                )
                .setNegativeButton("Отмена", null)
                .show();
    }

    // ============================================================
    // АКАДЕМИЯ
    // ============================================================

    void showCourses() {

        base("courses");

        content.addView(
                text(
                        "Академия института",
                        28,
                        NAVY,
                        true
                )
        );

        content.addView(
                card(
                        "Повышение квалификации",
                        "Ректор добавляет учебные материалы, ссылки и документы. В дальнейшем агент изучает материал, проходит тестирование и получает новую квалификацию."
                )
        );

        for (Course c : courses) {

            content.addView(
                    card(
                            c.title,
                            c.material +
                            "\nСтатус: " +
                            c.status
                    )
            );
        }

        content.addView(
                rowAction(
                        "＋",
                        "Создать курс",
                        "Добавить материал или ссылку",
                        this::addCourse
                )
        );
    }

    void addCourse() {

        LinearLayout box = dialogBox();

        EditText t = field("Название курса");
        EditText m = field(
                "Ссылка или название материала"
        );

        box.addView(t);
        box.addView(m);

        new AlertDialog.Builder(this)
                .setTitle("Повышение квалификации")
                .setView(box)
                .setPositiveButton(
                        "Назначить",
                        (d, w) -> {

                            String title =
                                    t.getText().toString().trim();

                            if (!title.isEmpty()) {

                                Course c =
                                        new Course(
                                                title,
                                                m.getText().toString()
                                        );

                                courses.add(c);

                                db.addCourse(
                                        title,
                                        c.material
                                );

                                showCourses();
                            }
                        }
                )
                .setNegativeButton("Отмена", null)
                .show();
    }

    // ============================================================
    // НАУЧНОЕ ПРОИЗВОДСТВО
    // ============================================================

    void showAssignments() {

        base("assignments");

        content.addView(
                text(
                        "Научное производство",
                        28,
                        NAVY,
                        true
                )
        );

        content.addView(
                card(
                        "Ректорское поручение",
                        "Ректор задаёт тему, тип продукции, объём и требования. Далее задача может быть передана в мультиагентный конвейер."
                )
        );

        for (Assignment a : assignments) {

            content.addView(
                    card(
                            a.type + "\n" + a.title,
                            "Статус: " + a.status +
                            "\nКафедры: " + a.departments +
                            "\nАгенты: " + a.agents
                    )
            );
        }

        content.addView(
                rowAction(
                        "＋",
                        "Новое научное поручение",
                        "НИР, статья, справка, доклад и другие продукты",
                        this::addAssignment
                )
        );
    }

    void addAssignment() {

        LinearLayout box = dialogBox();

        EditText title =
                field("Тема / название поручения");

        EditText type =
                field("Тип: НИР, статья, справка, доклад...");

        EditText dep =
                field("Кафедры");

        EditText ag =
                field(
                        "Агенты или: автоматически подобрать"
                );

        box.addView(title);
        box.addView(type);
        box.addView(dep);
        box.addView(ag);

        new AlertDialog.Builder(this)
                .setTitle("Новое научное поручение")
                .setView(box)
                .setPositiveButton(
                        "Создать",
                        (d, w) -> {

                            String t =
                                    title.getText()
                                            .toString()
                                            .trim();

                            if (!t.isEmpty()) {

                                Assignment a =
                                        new Assignment(
                                                t,
                                                type.getText().toString(),
                                                dep.getText().toString(),
                                                ag.getText().toString()
                                        );

                                assignments.add(a);

                                db.addAssignment(
                                        a.title,
                                        a.type,
                                        a.departments,
                                        a.agents
                                );

                                showAssignments();
                            }
                        }
                )
                .setNegativeButton("Отмена", null)
                .show();
    }

    // ============================================================
    // AI-ОРКЕСТРАТОР
    // ============================================================

    void showPipelines() {

        base("pipelines");

        content.addView(
                text(
                        "AI-оркестратор",
                        28,
                        NAVY,
                        true
                )
        );

        content.addView(
                card(
                        "Мультиагентный научный конвейер",
                        "Декомпозиция → подбор агентов → исследование → документы → критик → рецензирование → редактура → готовый научный продукт."
                )
        );

        for (final PipelineJob p : pipelineJobs) {

            TextView v =
                    card(
                            p.title,
                            "Тип: " + p.type +
                            "\nСтатус: " + p.status +
                            "\nОбъём: " + p.pages
                    );

            v.setOnClickListener(
                    x -> showPipelineDetail(p)
            );

            content.addView(v);
        }

        content.addView(
                rowAction(
                        "＋",
                        "Запустить научный конвейер",
                        "Создать новое ректорское задание",
                        this::addPipeline
                )
        );
    }

    void addPipeline() {

        LinearLayout box = dialogBox();

        EditText title =
                field("Название исследования / поручения");

        Spinner type = new Spinner(this);

        String[] types = {
                "НИР",
                "Научная статья",
                "Обзорная справка",
                "Аналитическая справка",
                "Обзор литературы",
                "Доклад",
                "Монография",
                "Диссертационное исследование",
                "Методические материалы",
                "Технический отчёт",
                "Другое"
        };

        type.setAdapter(
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        types
                )
        );

        EditText pages =
                field(
                        "Требуемый объём, страниц"
                );

        EditText standard =
                field(
                        "ВАК / ГОСТ / журнал / организация"
                );

        EditText deadline =
                field("Срок выполнения");

        EditText field =
                field("Научная область / кафедра");

        EditText web =
                field("Web Research: да/нет");

        EditText reviewers =
                field(
                        "Рецензирование: да/нет; число рецензентов"
                );

        box.addView(title);
        box.addView(type);
        box.addView(pages);
        box.addView(standard);
        box.addView(deadline);
        box.addView(field);
        box.addView(web);
        box.addView(reviewers);

        new AlertDialog.Builder(this)
                .setTitle("Ректорское научное поручение")
                .setView(box)
                .setPositiveButton(
                        "Создать",
                        (d, w) -> {

                            String t =
                                    title.getText()
                                            .toString()
                                            .trim();

                            if (!t.isEmpty()) {

                                PipelineJob p =
                                        new PipelineJob(
                                                t,
                                                type.getSelectedItem()
                                                        .toString(),
                                                pages.getText().toString(),
                                                standard.getText().toString(),
                                                deadline.getText().toString(),
                                                field.getText().toString(),
                                                web.getText().toString(),
                                                reviewers.getText().toString()
                                        );

                                pipelineJobs.add(p);

                                db.addPipeline(
                                        p.title,
                                        p.type,
                                        p.pages,
                                        p.standard,
                                        p.deadline,
                                        p.field,
                                        p.webResearch,
                                        p.reviewers
                                );

                                showPipelines();
                            }
                        }
                )
                .setNegativeButton("Отмена", null)
                .show();
    }

    void showPipelineDetail(final PipelineJob p) {

        base("pipeline");

        content.addView(
                text(
                        "Научный конвейер",
                        27,
                        NAVY,
                        true
                )
        );

        content.addView(
                card(
                        p.title,
                        "Тип продукции: " +
                        p.type +
                        "\nСтатус: " +
                        p.status
                )
        );

        content.addView(
                card(
                        "Техническое задание",
                        "Объём: " + p.pages +
                        "\nТребования: " + p.standard +
                        "\nСрок: " + p.deadline +
                        "\nОбласть: " + p.field +
                        "\nWeb Research: " + p.webResearch +
                        "\nРецензирование: " + p.reviewers
                )
        );

        String[] stages = {
                "1. Декомпозиция задачи",
                "2. Подбор научной группы",
                "3. Web Research",
                "4. Анализ документов",
                "5. Исследовательская работа",
                "6. Критическая проверка",
                "7. Рецензирование",
                "8. Научное редактирование",
                "9. Формирование продукции"
        };

        for (final String stage : stages) {

            content.addView(
                    rowAction(
                            "●",
                            stage,
                            "Нажмите, чтобы создать задачу агенту",
                            () -> addTaskForStage(p, stage)
                    )
            );
        }

        content.addView(
                rowAction(
                        "📐",
                        "План объёма и структуры",
                        "Разделы, страницы и слова",
                        () -> planPipeline(p)
                )
        );

        content.addView(
                rowAction(
                        "📋",
                        "Задачи агентов",
                        "Просмотр и изменение статусов",
                        () -> showTasksForPipeline(p)
                )
        );

        content.addView(
                rowAction(
                        "▶",
                        "Перевести в работу",
                        "Статус: В работе",
                        () -> {
                            p.status = "В работе";
                            db.updatePipelineStatus(
                                    p.title,
                                    p.status
                            );
                            showPipelineDetail(p);
                        }
                )
        );

        content.addView(
                rowAction(
                        "✓",
                        "Завершить",
                        "Научная продукция готова",
                        () -> {
                            p.status = "Завершено";
                            db.updatePipelineStatus(
                                    p.title,
                                    p.status
                            );
                            showPipelineDetail(p);
                        }
                )
        );
    }

    void addTaskForStage(
            final PipelineJob p,
            final String stage
    ) {

        LinearLayout box = dialogBox();

        EditText task =
                field("Задача для агента");

        EditText agent =
                field(
                        "Агент или: подобрать автоматически"
                );

        box.addView(task);
        box.addView(agent);

        Button suggest =
                smallButton(
                        "🧠 Подобрать по компетенциям"
                );

        suggest.setOnClickListener(
                v -> agent.setText(
                        suggestAgents(
                                task.getText().toString()
                        )
                )
        );

        box.addView(suggest);

        new AlertDialog.Builder(this)
                .setTitle(stage)
                .setView(box)
                .setPositiveButton(
                        "Создать",
                        (d, w) -> {

                            String t =
                                    task.getText()
                                            .toString()
                                            .trim();

                            if (!t.isEmpty()) {

                                String a =
                                        agent.getText()
                                                .toString();

                                AgentTask x =
                                        new AgentTask(
                                                p.title,
                                                stage,
                                                t,
                                                a
                                        );

                                agentTasks.add(x);

                                db.addAgentTask(
                                        p.title,
                                        stage,
                                        t,
                                        a
                                );

                                showPipelineDetail(p);
                            }
                        }
                )
                .setNegativeButton("Отмена", null)
                .show();
    }

    String suggestAgents(String task) {

        if (agents.size() == 0) {
            return "Нет зарегистрированных агентов";
        }

        String q =
                task == null
                        ? ""
                        : task.toLowerCase();

        StringBuilder out =
                new StringBuilder();

        for (Agent a : agents) {

            String c =
                    agentCompetencies.get(a.name);

            if (c == null) continue;

            String lc = c.toLowerCase();

            boolean match =
                    (q.contains("норм") &&
                     (lc.contains("прав") ||
                      lc.contains("закон"))) ||

                    (q.contains("дан") &&
                     (lc.contains("анал") ||
                      lc.contains("стат"))) ||

                    (q.contains("радио") &&
                     lc.contains("ради")) ||

                    (q.contains("безопас") &&
                     lc.contains("безопас")) ||

                    (q.contains("исслед") &&
                     lc.contains("исслед"));

            if (match) {

                if (out.length() > 0) {
                    out.append(", ");
                }

                out.append(a.name);
            }
        }

        if (out.length() == 0) {
            out.append(agents.get(0).name);
        }

        return out.toString();
    }

    // ============================================================
    // ПЛАН НИР
    // ============================================================

    void planPipeline(final PipelineJob p) {

        LinearLayout box = dialogBox();

        EditText sections =
                field("Количество разделов");

        EditText pages =
                field("Страниц на раздел");

        EditText words =
                field("Ориентир слов на страницу");

        box.addView(sections);
        box.addView(pages);
        box.addView(words);

        new AlertDialog.Builder(this)
                .setTitle("Планирование научного объёма")
                .setView(box)
                .setPositiveButton(
                        "Сформировать",
                        (d, w) -> {

                            int n = 1;
                            int pg = 1;
                            int wp = 300;

                            try {
                                n = Integer.parseInt(
                                        sections.getText()
                                                .toString()
                                );
                            } catch (Exception ignored) {}

                            try {
                                pg = Integer.parseInt(
                                        pages.getText()
                                                .toString()
                                );
                            } catch (Exception ignored) {}

                            try {
                                wp = Integer.parseInt(
                                        words.getText()
                                                .toString()
                                );
                            } catch (Exception ignored) {}

                            for (int i = 1; i <= n; i++) {

                                String title =
                                        "Раздел " + i;

                                ResearchSection rs =
                                        new ResearchSection(
                                                p.title,
                                                title,
                                                String.valueOf(pg),
                                                String.valueOf(pg * wp),
                                                "Подобрать автоматически"
                                        );

                                researchSections.add(rs);

                                db.addSection(
                                        p.title,
                                        title,
                                        String.valueOf(pg),
                                        String.valueOf(pg * wp),
                                        "Подобрать автоматически"
                                );
                            }

                            showPlan(p);
                        }
                )
                .setNegativeButton("Отмена", null)
                .show();
    }

    void showPlan(final PipelineJob p) {

        base("plan");

        content.addView(
                text(
                        "План исследования",
                        27,
                        NAVY,
                        true
                )
        );

        int totalPages = 0;
        int totalWords = 0;
        int count = 0;

        for (ResearchSection r : researchSections) {

            if (!r.pipeline.equals(p.title)) {
                continue;
            }

            count++;

            try {
                totalPages +=
                        Integer.parseInt(r.pages);
            } catch (Exception ignored) {}

            try {
                totalWords +=
                        Integer.parseInt(r.words);
            } catch (Exception ignored) {}

            content.addView(
                    card(
                            r.title,
                            "Объём: " +
                            r.pages +
                            " стр.\n" +
                            r.words +
                            " слов\nАгент: " +
                            r.agent
                    )
            );
        }

        content.addView(
                card(
                        "Итого",
                        "Разделов: " + count +
                        "\nПлановый объём: " +
                        totalPages +
                        " страниц" +
                        "\nОриентир: " +
                        totalWords +
                        " слов"
                )
        );

        content.addView(
                rowAction(
                        "＋",
                        "Добавить раздел",
                        "Расширить план НИР",
                        () -> addSection(p)
                )
        );
    }

    void addSection(final PipelineJob p) {

        LinearLayout box = dialogBox();

        EditText t = field("Название раздела");
        EditText pg = field("Страницы");
        EditText w = field("Слова");
        EditText a = field("Агент");

        box.addView(t);
        box.addView(pg);
        box.addView(w);
        box.addView(a);

        new AlertDialog.Builder(this)
                .setTitle("Раздел исследования")
                .setView(box)
                .setPositiveButton(
                        "Добавить",
                        (d, x) -> {

                            if (!t.getText()
                                    .toString()
                                    .trim()
                                    .isEmpty()) {

                                ResearchSection r =
                                        new ResearchSection(
                                                p.title,
                                                t.getText().toString(),
                                                pg.getText().toString(),
                                                w.getText().toString(),
                                                a.getText().toString()
                                        );

                                researchSections.add(r);

                                db.addSection(
                                        p.title,
                                        r.title,
                                        r.pages,
                                        r.words,
                                        r.agent
                                );

                                showPlan(p);
                            }
                        }
                )
                .setNegativeButton("Отмена", null)
                .show();
    }

    void showTasksForPipeline(final PipelineJob p) {

        base("tasks");

        content.addView(
                text(
                        "Задачи агентов",
                        27,
                        NAVY,
                        true
                )
        );

        boolean any = false;

        for (final AgentTask t : agentTasks) {

            if (!t.pipeline.equals(p.title)) {
                continue;
            }

            any = true;

            TextView v =
                    card(
                            t.stage,
                            t.task +
                            "\nАгент: " +
                            t.agent +
                            "\nСтатус: " +
                            t.status
                    );

            v.setOnClickListener(
                    x -> editAgentTask(t, p)
            );

            content.addView(v);
        }

        if (!any) {

            content.addView(
                    card(
                            "Задач пока нет",
                            "Откройте этап конвейера и создайте задачу агенту."
                    )
            );
        }
    }

    void editAgentTask(
            final AgentTask t,
            final PipelineJob p
    ) {

        final String[] statuses = {
                "Ожидает",
                "В работе",
                "На проверке",
                "Доработка",
                "Принято",
                "Отклонено"
        };

        int selected = 0;

        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equals(t.status)) {
                selected = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(t.task)
                .setSingleChoiceItems(
                        statuses,
                        selected,
                        (d, w) -> {

                            t.status =
                                    statuses[w];

                            db.updateAgentTaskStatus(
                                    t.task,
                                    t.status
                            );

                            d.dismiss();

                            showTasksForPipeline(p);
                        }
                )
                .setNegativeButton("Отмена", null)
                .show();
    }

    // ============================================================
    // ДОКУМЕНТЫ
    // ============================================================

    void showDocuments() {

        base("documents");

        content.addView(
                text(
                        "Документы института",
                        28,
                        NAVY,
                        true
                )
        );

        content.addView(
                card(
                        "Документальный контур",
                        "PDF, DOCX и другие файлы ректора. Следующий слой системы будет извлекать текст, индексировать документы и передавать релевантные фрагменты агентам."
                )
        );

        for (DocumentItem d : documents) {

            TextView v =
                    card(
                            d.name,
                            d.category +
                            "\nСвязь: " +
                            d.linkedTo
                    );

            v.setOnClickListener(
                    x -> showDocumentDetail(d)
            );

            content.addView(v);
        }

        content.addView(
                rowAction(
                        "🔎",
                        "Поиск по документам",
                        "Название, категория и связь",
                        this::searchDocuments
                )
        );

        content.addView(
                rowAction(
                        "＋",
                        "Загрузить документ",
                        "Выбрать файл из памяти телефона",
                        this::pickDocument
                )
        );
    }

    void pickDocument() {

        Intent i =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                );

        i.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        i.setType("*/*");

        startActivityForResult(
                i,
                PICK_DOCUMENT
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (
                requestCode == PICK_DOCUMENT &&
                resultCode == RESULT_OK &&
                data != null &&
                data.getData() != null
        ) {

            Uri u = data.getData();

            try {

                getContentResolver()
                        .takePersistableUriPermission(
                                u,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

            } catch (Exception ignored) {}

            String name =
                    u.getLastPathSegment();

            if (name == null) {
                name = "Документ";
            }

            String uri =
                    u.toString();

            DocumentItem d =
                    new DocumentItem(
                            name,
                            uri,
                            "Не классифицирован",
                            "Не назначен"
                    );

            documents.add(d);

            db.addDocument(
                    name,
                    uri,
                    d.category,
                    d.linkedTo
            );

            showDocuments();
        }
    }

    void searchDocuments() {

        final EditText q =
                field(
                        "Введите тему, термин или фразу"
                );

        new AlertDialog.Builder(this)
                .setTitle("Поиск по документам")
                .setView(q)
                .setPositiveButton(
                        "Искать",
                        (d, w) -> {

                            String query =
                                    q.getText()
                                            .toString()
                                            .trim()
                                            .toLowerCase();

                            base("search");

                            content.addView(
                                    text(
                                            "Результаты поиска",
                                            27,
                                            NAVY,
                                            true
                                    )
                            );

                            if (query.isEmpty()) {

                                content.addView(
                                        card(
                                                "Поиск",
                                                "Введите запрос."
                                        )
                                );

                                return;
                            }

                            boolean found = false;

                            for (DocumentItem x :
                                    documents) {

                                if (
                                        x.name
                                                .toLowerCase()
                                                .contains(query)
                                        ||
                                        x.category
                                                .toLowerCase()
                                                .contains(query)
                                        ||
                                        x.linkedTo
                                                .toLowerCase()
                                                .contains(query)
                                ) {

                                    found = true;

                                    content.addView(
                                            card(
                                                    x.name,
                                                    "Категория: " +
                                                    x.category +
                                                    "\nСвязь: " +
                                                    x.linkedTo
                                            )
                                    );
                                }
                            }

                            if (!found) {

                                content.addView(
                                        card(
                                                "Ничего не найдено",
                                                "Полнотекстовый и смысловой поиск по содержимому PDF/DOCX будет подключён через локальный индекс документов."
                                        )
                                );
                            }
                        }
                )
                .setNegativeButton("Отмена", null)
                .show();
    }

    void showDocumentDetail(
            final DocumentItem d
    ) {

        base("document");

        content.addView(
                text(
                        "Карточка документа",
                        27,
                        NAVY,
                        true
                )
        );

        content.addView(
                card(
                        d.name,
                        "Категория: " +
                        d.category +
                        "\nСвязан с: " +
                        d.linkedTo
                )
        );

        content.addView(
                rowAction(
                        "✎",
                        "Классифицировать",
                        "Связать с агентом, кафедрой или НИР",
                        () -> editDocument(d)
                )
        );

        content.addView(
                rowAction(
                        "📖",
                        "Открыть файл",
                        "Передать файл соответствующему приложению",
                        () -> {

                            try {

                                Intent i =
                                        new Intent(
                                                Intent.ACTION_VIEW
                                        );

                                i.setDataAndType(
                                        Uri.parse(d.uri),
                                        "*/*"
                                );

                                i.addFlags(
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );

                                startActivity(i);

                            } catch (Exception e) {

                                info(
                                        "Не удалось открыть",
                                        "Откройте файл через файловый менеджер телефона."
                                );
                            }
                        }
                )
        );
    }

    void editDocument(
            final DocumentItem d
    ) {

        LinearLayout box = dialogBox();

        EditText c =
                field(
                        "Категория: учебный материал / НИР / нормативный документ / статья"
                );

        c.setText(d.category);

        EditText l =
                field(
                        "Связь: агент / кафедра / курс / НИР"
                );

        l.setText(d.linkedTo);

        box.addView(c);
        box.addView(l);

        new AlertDialog.Builder(this)
                .setTitle("Классификация документа")
                .setView(box)
                .setPositiveButton(
                        "Сохранить",
                        (x, w) -> {

                            d.category =
                                    c.getText().toString();

                            d.linkedTo =
                                    l.getText().toString();

                            db.updateDocument(
                                    d.name,
                                    d.category,
                                    d.linkedTo
                            );

                            showDocumentDetail(d);
                        }
                )
                .setNegativeButton("Отмена", null)
                .show();
    }

    // ============================================================
    // WEB RESEARCH
    // ============================================================

    void showResearch() {

        base("research");

        content.addView(
                text(
                        "Web Research",
                        28,
                        NAVY,
                        true
                )
        );

        content.addView(
                card(
                        "Исследовательский режим",
                        "Ректор задаёт тему, ограничения, период и тип источников. В дальнейшем локальный/серверный исследователь будет собирать доказательную базу."
                )
        );

        for (ResearchJob j : researchJobs) {

            content.addView(
                    card(
                            j.topic,
                            "Область: " +
                            j.scope +
                            "\nСтатус: " +
                            j.status
                    )
            );
        }

        content.addView(
                rowAction(
                        "＋",
                        "Новое исследование",
                        "Задать тему и параметры поиска",
                        this::addResearch
                )
        );
    }

    void addResearch() {

        LinearLayout box = dialogBox();

        EditText t =
                field("Тема исследования");

        EditText scope =
                field(
                        "Что искать / ограничения / период / источники"
                );

        box.addView(t);
        box.addView(scope);

        new AlertDialog.Builder(this)
                .setTitle("Web Research")
                .setView(box)
                .setPositiveButton(
                        "Создать",
                        (d, w) -> {

                            String topic =
                                    t.getText()
                                            .toString()
                                            .trim();

                            if (!topic.isEmpty()) {

                                ResearchJob r =
                                        new ResearchJob(
                                                topic,
                                                scope.getText()
                                                        .toString()
                                        );

                                researchJobs.add(r);

                                db.addResearch(
                                        r.topic,
                                        r.scope
                                );

                                showResearch();
                            }
                        }
                )
                .setNegativeButton("Отмена", null)
                .show();
    }

    // ============================================================
    // ИСТОЧНИКИ
    // ============================================================

    void showSources() {

        base("sources");

        content.addView(
                text(
                        "Источники знаний",
                        28,
                        NAVY,
                        true
                )
        );

        for (Source s : sources) {

            content.addView(
                    card(
                            s.title,
                            s.kind +
                            "\n" +
                            s.url
                    )
            );
        }

        content.addView(
                rowAction(
                        "＋",
                        "Добавить источник",
                        "URL, PDF, DOCX или нормативный документ",
                        this::addSource
                )
        );
    }

    void addSource() {

        LinearLayout box = dialogBox();

        EditText t =
                field("Название источника");

        EditText u =
                field("URL или имя файла");

        EditText k =
                field(
                        "Тип: интернет / PDF / DOCX / нормативный документ"
                );

        box.addView(t);
        box.addView(u);
        box.addView(k);

        new AlertDialog.Builder(this)
                .setTitle("Источник знаний")
                .setView(box)
                .setPositiveButton(
                        "Сохранить",
                        (d, w) -> {

                            String title =
                                    t.getText()
                                            .toString()
                                            .trim();

                            if (!title.isEmpty()) {

                                Source s =
                                        new Source(
                                                title,
                                                u.getText().toString(),
                                                k.getText().toString()
                                        );

                                sources.add(s);

                                db.addSource(
                                        s.title,
                                        s.url,
                                        s.kind
                                );

                                showSources();
                            }
                        }
                )
                .setNegativeButton("Отмена", null)
                .show();
    }

    // ============================================================
    // СОРЕВНОВАНИЯ
    // ============================================================

    void showCompetitions() {

        base("competitions");

        content.addView(
                text(
                        "Соревнования агентов",
                        28,
                        NAVY,
                        true
                )
        );

        content.addView(
                card(
                        "Научное соревнование",
                        "Одинаковая задача передаётся нескольким агентам. Результаты сравниваются и проходят независимое рецензирование."
                )
        );

        for (Competition c :
                competitions) {

            content.addView(
                    card(
                            c.title,
                            "Задача: " +
                            c.task +
                            "\nУчастники: " +
                            c.participants +
                            "\nСтатус: " +
                            c.status
                    )
            );
        }

        content.addView(
                rowAction(
                        "＋",
                        "Новое соревнование",
                        "Сравнить результаты научных агентов",
                        this::addCompetition
                )
        );
    }

    void addCompetition() {

        LinearLayout box = dialogBox();

        EditText t =
                field("Название соревнования");

        EditText task =
                field("Одинаковая научная задача");

        EditText p =
                field(
                        "Участники: агенты через запятую"
                );

        box.addView(t);
        box.addView(task);
        box.addView(p);

        new AlertDialog.Builder(this)
                .setTitle("Соревнование агентов")
                .setView(box)
                .setPositiveButton(
                        "Создать",
                        (d, w) -> {

                            String title =
                                    t.getText()
                                            .toString()
                                            .trim();

                            if (!title.isEmpty()) {

                                Competition c =
                                        new Competition(
                                                title,
                                                task.getText().toString(),
                                                p.getText().toString()
                                        );

                                competitions.add(c);

                                db.addCompetition(
                                        c.title,
                                        c.task,
                                        c.participants
                                );

                                showCompetitions();
                            }
                        }
                )
                .setNegativeButton("Отмена", null)
                .show();
    }

    // ============================================================
    // ЛОКАЛЬНЫЙ QWEN
    // ============================================================

    void testLocalQwen() {

        final EditText input =
                field(
                        "Например: составь план научной статьи по ИИ"
                );

        new AlertDialog.Builder(this)
                .setTitle("Локальный Qwen3 8B")
                .setView(input)
                .setPositiveButton(
                        "Отправить",
                        (d, w) -> {

                            String prompt =
                                    input.getText()
                                            .toString()
                                            .trim();

                            if (prompt.isEmpty()) {
                                return;
                            }

                            final ProgressDialog pd =
                                    new ProgressDialog(this);

                            pd.setMessage(
                                    "Локальный Qwen думает..."
                            );

                            pd.setCancelable(false);
                            pd.show();

                            new Thread(() -> {

                                String result =
                                        callLocalQwen(prompt);

                                runOnUiThread(() -> {

                                    if (pd.isShowing()) {
                                        pd.dismiss();
                                    }

                                    new AlertDialog.Builder(this)
                                            .setTitle(
                                                    "Ответ локального Qwen"
                                            )
                                            .setMessage(result)
                                            .setPositiveButton(
                                                    "Понятно",
                                                    null
                                            )
                                            .show();
                                });

                            }).start();
                        }
                )
                .setNegativeButton(
                        "Отмена",
                        null
                )
                .show();
    }

    String callLocalQwen(String prompt) {

        String urlString =
                "http://127.0.0.1:8080/v1/chat/completions";

        HttpURLConnection conn = null;

        try {

            URL url =
                    new URL(urlString);

            conn =
                    (HttpURLConnection)
                            url.openConnection();

            conn.setConnectTimeout(15000);
            conn.setReadTimeout(120000);

            conn.setRequestMethod("POST");

            conn.setRequestProperty(
                    "Content-Type",
                    "application/json"
            );

            conn.setDoOutput(true);

            String safe =
                    escapeJson(prompt);

            String json =
                    "{"
                    + "\"model\":\"Qwen3-8B-Q4_K_M.gguf\","
                    + "\"messages\":["
                    + "{"
                    + "\"role\":\"user\","
                    + "\"content\":\""
                    + safe
                    + "\""
                    + "}"
                    + "],"
                    + "\"temperature\":0.7,"
                    + "\"max_tokens\":4096"
                    + "}";

            OutputStream os =
                    conn.getOutputStream();

            os.write(
                    json.getBytes("UTF-8")
            );

            os.flush();
            os.close();

            int code =
                    conn.getResponseCode();

            InputStream stream;

            if (code >= 200 && code < 300) {
                stream =
                        conn.getInputStream();
            } else {
                stream =
                        conn.getErrorStream();
            }

            String response =
                    readStream(stream);

            if (code < 200 || code >= 300) {

                return "Ошибка локального Qwen.\n\n"
                        + "HTTP "
                        + code
                        + "\n\n"
                        + response;
            }

            String result =
                    extractJsonContent(response);

            if (result == null ||
                    result.trim().isEmpty()) {

                return response;
            }

            return result;

        } catch (java.net.ConnectException e) {

            return
                    "Не удалось подключиться к Qwen.\n\n"
                    + "Проверьте, что локальный сервер запущен "
                    + "на 127.0.0.1:8080.";

        } catch (java.net.SocketTimeoutException e) {

            return
                    "Qwen отвечает слишком долго.\n\n"
                    + "Модель работает, но генерация ещё не успела завершиться. "
                    + "Для первого теста используйте короткий запрос.";

        } catch (Exception e) {

            return
                    "Ошибка локального Qwen:\n"
                    + e.toString();

        } finally {

            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    String readStream(InputStream stream)
            throws Exception {

        if (stream == null) {
            return "";
        }

        BufferedReader br =
                new BufferedReader(
                        new InputStreamReader(
                                stream,
                                "UTF-8"
                        )
                );

        StringBuilder sb =
                new StringBuilder();

        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        br.close();

        return sb.toString();
    }

    String escapeJson(String s) {

        if (s == null) {
            return "";
        }

        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    String extractJsonContent(String json) {

        String marker =
                "\"content\":\"";

        int start =
                json.indexOf(marker);

        if (start < 0) {
            return null;
        }

        start += marker.length();

        StringBuilder out =
                new StringBuilder();

        boolean escaped = false;

        for (int i = start;
             i < json.length();
             i++) {

            char ch =
                    json.charAt(i);

            if (escaped) {

                switch (ch) {

                    case 'n':
                        out.append('\n');
                        break;

                    case 'r':
                        out.append('\r');
                        break;

                    case 't':
                        out.append('\t');
                        break;

                    case '"':
                        out.append('"');
                        break;

                    case '\\':
                        out.append('\\');
                        break;

                    default:
                        out.append(ch);
                        break;
                }

                escaped = false;

            } else if (ch == '\\') {

                escaped = true;

            } else if (ch == '"') {

                break;

            } else {

                out.append(ch);
            }
        }

        return out.toString();
    }

    // ============================================================
    // НАСТРОЙКИ ИИ
    // ============================================================

    void aiSettings() {

        LinearLayout box =
                dialogBox();

        EditText key =
                field(
                        "Gemini API key (не хранится в коде)"
                );

        key.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        key.setText(
                prefs.getString(
                        "gemini_key",
                        ""
                )
        );

        box.addView(key);

        new AlertDialog.Builder(this)
                .setTitle("Настройки ИИ")
                .setView(box)
                .setPositiveButton(
                        "Сохранить",
                        (d, w) -> {

                            prefs.edit()
                                    .putString(
                                            "gemini_key",
                                            key.getText()
                                                    .toString()
                                    )
                                    .apply();

                            info(
                                    "Сохранено",
                                    "Ключ Gemini теперь хранится в настройках приложения, а не в исходном коде."
                            );
                        }
                )
                .setNegativeButton(
                        "Отмена",
                        null
                )
                .show();
    }

    // ============================================================
    // GEMINI — БЕЗ КЛЮЧА В ИСХОДНИКЕ
    // ============================================================

    void testGemini() {

        final EditText input =
                field("Введите запрос для Gemini");

        new AlertDialog.Builder(this)
                .setTitle("Тест Gemini")
                .setView(input)
                .setPositiveButton(
                        "Отправить",
                        (d, w) -> {

                            String prompt =
                                    input.getText()
                                            .toString()
                                            .trim();

                            if (prompt.isEmpty()) {
                                return;
                            }

                            String key =
                                    prefs.getString(
                                            "gemini_key",
                                            ""
                                    );

                            if (key.isEmpty()) {

                                new AlertDialog.Builder(this)
                                        .setTitle(
                                                "Нет ключа Gemini"
                                        )
                                        .setMessage(
                                                "Откройте Настройки ИИ и сохраните новый API-ключ."
                                        )
                                        .setPositiveButton(
                                                "Настройки",
                                                (x, y) ->
                                                        aiSettings()
                                        )
                                        .setNegativeButton(
                                                "Отмена",
                                                null
                                        )
                                        .show();

                                return;
                            }

                            final ProgressDialog pd =
                                    new ProgressDialog(this);

                            pd.setMessage("Gemini думает...");
                            pd.show();

                            new Thread(() -> {

                                String result =
                                        callGemini(
                                                prompt,
                                                key
                                        );

                                runOnUiThread(() -> {

                                    if (pd.isShowing()) {
                                        pd.dismiss();
                                    }

                                    new AlertDialog.Builder(this)
                                            .setTitle(
                                                    "Ответ Gemini"
                                            )
                                            .setMessage(result)
                                            .setPositiveButton(
                                                    "OK",
                                                    null
                                            )
                                            .show();
                                });

                            }).start();
                        }
                )
                .setNegativeButton(
                        "Отмена",
                        null
                )
                .show();
    }

    String callGemini(
            String prompt,
            String apiKey
    ) {

        HttpURLConnection conn = null;

        try {

            String model =
                    "gemini-flash-latest";

            String urlString =
                    "https://generativelanguage.googleapis.com/v1beta/models/"
                    + model
                    + ":generateContent";

            URL url =
                    new URL(urlString);

            conn =
                    (HttpURLConnection)
                            url.openConnection();

            conn.setConnectTimeout(15000);
            conn.setReadTimeout(120000);

            conn.setRequestMethod("POST");

            conn.setRequestProperty(
                    "Content-Type",
                    "application/json"
            );

            conn.setRequestProperty(
                    "X-goog-api-key",
                    apiKey
            );

            conn.setDoOutput(true);

            String json =
                    "{"
                    + "\"contents\":["
                    + "{"
                    + "\"parts\":["
                    + "{"
                    + "\"text\":\""
                    + escapeJson(prompt)
                    + "\""
                    + "}"
                    + "]"
                    + "}"
                    + "]"
                    + "}";

            OutputStream os =
                    conn.getOutputStream();

            os.write(
                    json.getBytes("UTF-8")
            );

            os.flush();
            os.close();

            int code =
                    conn.getResponseCode();

            InputStream stream =
                    code >= 200 && code < 300
                            ? conn.getInputStream()
                            : conn.getErrorStream();

            String response =
                    readStream(stream);

            if (code < 200 || code >= 300) {

                return
                        "HTTP "
                        + code
                        + "\n\n"
                        + response;
            }

            String result =
                    extractJsonText(response);

            return result == null
                    ? response
                    : result;

        } catch (Exception e) {

            return
                    "Ошибка Gemini:\n"
                    + e.toString();

        } finally {

            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    String extractJsonText(String json) {

        String marker =
                "\"text\":\"";

        int start =
                json.indexOf(marker);

        if (start < 0) {
            return null;
        }

        start += marker.length();

        StringBuilder out =
                new StringBuilder();

        boolean escaped = false;

        for (int i = start;
             i < json.length();
             i++) {

            char ch =
                    json.charAt(i);

            if (escaped) {

                if (ch == 'n') {
                    out.append('\n');
                } else if (ch == 'r') {
                    out.append('\r');
                } else if (ch == 't') {
                    out.append('\t');
                } else if (ch == '"') {
                    out.append('"');
                } else if (ch == '\\') {
                    out.append('\\');
                } else {
                    out.append(ch);
                }

                escaped = false;

            } else if (ch == '\\') {

                escaped = true;

            } else if (ch == '"') {

                break;

            } else {

                out.append(ch);
            }
        }

        return out.toString();
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЕ UI
    // ============================================================

    LinearLayout dialogBox() {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                dp(25),
                dp(4),
                dp(25),
                0
        );

        return box;
    }

    EditText field(String hint) {

        EditText e =
                new EditText(this);

        e.setHint(hint);
        e.setTextSize(14);
        e.setTextColor(TEXT);
        e.setHintTextColor(MUTED);
        e.setPadding(
                dp(4),
                dp(8),
                dp(4),
                dp(8)
        );

        return e;
    }

    GradientDrawable roundDrawable(
            int color,
            int radius
    ) {

        GradientDrawable g =
                new GradientDrawable();

        g.setColor(color);
        g.setCornerRadius(dp(radius));

        if (color == CARD) {
            g.setStroke(
                    dp(1),
                    BORDER
            );
        }

        return g;
    }

    GradientDrawable roundGradient(
            int c1,
            int c2,
            int radius
    ) {

        GradientDrawable g =
                new GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        new int[]{c1, c2}
                );

        g.setCornerRadius(
                dp(radius)
        );

        return g;
    }

    int dp(int value) {

        return (int)
                (value *
                        getResources()
                                .getDisplayMetrics()
                                .density +
                        0.5f);
    }

    void info(String title, String message) {

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                        "Понятно",
                        null
                )
                .show();
    }

    // ============================================================
    // SQLITE
    // ============================================================

    static class DB
            extends SQLiteOpenHelper {

        DB(Context c) {

            super(
                    c,
                    "institute.db",
                    null,
                    10
            );
        }

        @Override
        public void onCreate(
                SQLiteDatabase d
        ) {

            d.execSQL(
                    "CREATE TABLE departments(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "description TEXT)"
            );

            d.execSQL(
                    "CREATE TABLE agents(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "role TEXT," +
                    "department TEXT)"
            );

            d.execSQL(
                    "CREATE TABLE assignments(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT," +
                    "type TEXT," +
                    "department TEXT," +
                    "agents TEXT," +
                    "status TEXT)"
            );

            d.execSQL(
                    "CREATE TABLE courses(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT," +
                    "material TEXT," +
                    "status TEXT)"
            );

            d.execSQL(
                    "CREATE TABLE sources(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT," +
                    "url TEXT," +
                    "kind TEXT)"
            );

            d.execSQL(
                    "CREATE TABLE audit(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "action TEXT," +
                    "created_at INTEGER)"
            );

            d.execSQL(
                    "CREATE TABLE agent_profiles(" +
                    "agent_name TEXT PRIMARY KEY," +
                    "qualification TEXT," +
                    "competencies TEXT)"
            );

            d.execSQL(
                    "CREATE TABLE documents(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "uri TEXT," +
                    "category TEXT," +
                    "linked_to TEXT," +
                    "created_at INTEGER)"
            );

            d.execSQL(
                    "CREATE TABLE research_jobs(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "topic TEXT," +
                    "scope TEXT," +
                    "status TEXT," +
                    "created_at INTEGER)"
            );

            d.execSQL(
                    "CREATE TABLE pipeline_jobs(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT," +
                    "type TEXT," +
                    "status TEXT," +
                    "created_at INTEGER)"
            );

            d.execSQL(
                    "CREATE TABLE agent_tasks(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "pipeline_title TEXT," +
                    "stage TEXT," +
                    "task TEXT," +
                    "agent TEXT," +
                    "status TEXT," +
                    "created_at INTEGER)"
            );

            d.execSQL(
                    "CREATE TABLE competitions(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT," +
                    "task TEXT," +
                    "participants TEXT," +
                    "status TEXT," +
                    "created_at INTEGER)"
            );

            d.execSQL(
                    "CREATE TABLE pipeline_specs(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "pipeline_title TEXT UNIQUE," +
                    "type TEXT," +
                    "pages TEXT," +
                    "standard TEXT," +
                    "deadline TEXT," +
                    "field TEXT," +
                    "web_research TEXT," +
                    "reviewers TEXT)"
            );

            d.execSQL(
                    "CREATE TABLE research_sections(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "pipeline_title TEXT," +
                    "section_title TEXT," +
                    "pages TEXT," +
                    "words TEXT," +
                    "agent TEXT)"
            );
        }

        @Override
        public void onUpgrade(
                SQLiteDatabase d,
                int oldV,
                int newV
        ) {

            if (oldV < 2)
                d.execSQL(
                        "CREATE TABLE IF NOT EXISTS agent_profiles(" +
                        "agent_name TEXT PRIMARY KEY," +
                        "qualification TEXT," +
                        "competencies TEXT)"
                );

            if (oldV < 3)
                d.execSQL(
                        "CREATE TABLE IF NOT EXISTS documents(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT," +
                        "uri TEXT," +
                        "category TEXT," +
                        "linked_to TEXT," +
                        "created_at INTEGER)"
                );

            if (oldV < 4)
                d.execSQL(
                        "CREATE TABLE IF NOT EXISTS research_jobs(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "topic TEXT," +
                        "scope TEXT," +
                        "status TEXT," +
                        "created_at INTEGER)"
                );

            if (oldV < 5)
                d.execSQL(
                        "CREATE TABLE IF NOT EXISTS pipeline_jobs(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT," +
                        "type TEXT," +
                        "status TEXT," +
                        "created_at INTEGER)"
                );

            if (oldV < 6)
                d.execSQL(
                        "CREATE TABLE IF NOT EXISTS agent_tasks(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "pipeline_title TEXT," +
                        "stage TEXT," +
                        "task TEXT," +
                        "agent TEXT," +
                        "status TEXT," +
                        "created_at INTEGER)"
                );

            if (oldV < 7)
                d.execSQL(
                        "CREATE TABLE IF NOT EXISTS competitions(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT," +
                        "task TEXT," +
                        "participants TEXT," +
                        "status TEXT," +
                        "created_at INTEGER)"
                );

            if (oldV < 8)
                d.execSQL(
                        "CREATE TABLE IF NOT EXISTS pipeline_specs(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "pipeline_title TEXT UNIQUE," +
                        "type TEXT," +
                        "pages TEXT," +
                        "standard TEXT," +
                        "deadline TEXT," +
                        "field TEXT," +
                        "web_research TEXT," +
                        "reviewers TEXT)"
                );

            if (oldV < 9)
                d.execSQL(
                        "CREATE TABLE IF NOT EXISTS research_sections(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "pipeline_title TEXT," +
                        "section_title TEXT," +
                        "pages TEXT," +
                        "words TEXT," +
                        "agent TEXT)"
                );
        }

        void log(String action) {

            ContentValues v =
                    new ContentValues();

            v.put(
                    "action",
                    action
            );

            v.put(
                    "created_at",
                    System.currentTimeMillis()
            );

            getWritableDatabase()
                    .insert(
                            "audit",
                            null,
                            v
                    );
        }

        void addDepartment(
                String n,
                String d
        ) {

            ContentValues v =
                    new ContentValues();

            v.put("name", n);
            v.put("description", d);

            getWritableDatabase()
                    .insert(
                            "departments",
                            null,
                            v
                    );

            log("Создана кафедра: " + n);
        }

        void addAgent(
                String n,
                String r,
                String d
        ) {

            ContentValues v =
                    new ContentValues();

            v.put("name", n);
            v.put("role", r);
            v.put("department", d);

            getWritableDatabase()
                    .insert(
                            "agents",
                            null,
                            v
                    );

            log("Создан агент: " + n);
        }

        void addAssignment(
                String t,
                String ty,
                String d,
                String a
        ) {

            ContentValues v =
                    new ContentValues();

            v.put("title", t);
            v.put("type", ty);
            v.put("department", d);
            v.put("agents", a);
            v.put("status", "Новое");

            getWritableDatabase()
                    .insert(
                            "assignments",
                            null,
                            v
                    );

            log(
                    "Создано научное поручение: "
                    + t
            );
        }

        void addCourse(
                String t,
                String m
        ) {

            ContentValues v =
                    new ContentValues();

            v.put("title", t);
            v.put("material", m);
            v.put("status", "Назначен");

            getWritableDatabase()
                    .insert(
                            "courses",
                            null,
                            v
                    );

            log("Создан курс: " + t);
        }

        void saveAgentProfile(
                String n,
                String q,
                String c
        ) {

            ContentValues v =
                    new ContentValues();

            v.put("agent_name", n);
            v.put("qualification", q);
            v.put("competencies", c);

            getWritableDatabase()
                    .insertWithOnConflict(
                            "agent_profiles",
                            null,
                            v,
                            SQLiteDatabase.CONFLICT_REPLACE
                    );

            log(
                    "Изменён профиль агента: "
                    + n
            );
        }

        void addDocument(
                String n,
                String u,
                String c,
                String l
        ) {

            ContentValues v =
                    new ContentValues();

            v.put("name", n);
            v.put("uri", u);
            v.put("category", c);
            v.put("linked_to", l);
            v.put(
                    "created_at",
                    System.currentTimeMillis()
            );

            getWritableDatabase()
                    .insert(
                            "documents",
                            null,
                            v
                    );

            log("Загружен документ: " + n);
        }

        void updateDocument(
                String n,
                String c,
                String l
        ) {

            ContentValues v =
                    new ContentValues();

            v.put("category", c);
            v.put("linked_to", l);

            getWritableDatabase()
                    .update(
                            "documents",
                            v,
                            "name=?",
                            new String[]{n}
                    );

            log(
                    "Изменён документ: "
                    + n
            );
        }

        void addResearch(
                String t,
                String sc
        ) {

            ContentValues v =
                    new ContentValues();

            v.put("topic", t);
            v.put("scope", sc);
            v.put("status", "Подготовлено");
            v.put(
                    "created_at",
                    System.currentTimeMillis()
            );

            getWritableDatabase()
                    .insert(
                            "research_jobs",
                            null,
                            v
                    );

            log(
                    "Создан Web Research: "
                    + t
            );
        }

        void addPipeline(
                String t,
                String ty,
                String pg,
                String st,
                String dl,
                String f,
                String wr,
                String rv
        ) {

            ContentValues v =
                    new ContentValues();

            v.put("title", t);
            v.put("type", ty);
            v.put("status", "Создано");
            v.put(
                    "created_at",
                    System.currentTimeMillis()
            );

            getWritableDatabase()
                    .insert(
                            "pipeline_jobs",
                            null,
                            v
                    );

            ContentValues x =
                    new ContentValues();

            x.put("pipeline_title", t);
            x.put("type", ty);
            x.put("pages", pg);
            x.put("standard", st);
            x.put("deadline", dl);
            x.put("field", f);
            x.put("web_research", wr);
            x.put("reviewers", rv);

            getWritableDatabase()
                    .insertWithOnConflict(
                            "pipeline_specs",
                            null,
                            x,
                            SQLiteDatabase.CONFLICT_REPLACE
                    );

            log(
                    "Создано ректорское научное поручение: "
                    + t
            );
        }

        void updatePipelineStatus(
                String t,
                String st
        ) {

            ContentValues v =
                    new ContentValues();

            v.put("status", st);

            getWritableDatabase()
                    .update(
                            "pipeline_jobs",
                            v,
                            "title=?",
                            new String[]{t}
                    );

            log(
                    "Статус конвейера: "
                    + t
                    + " -> "
                    + st
            );
        }

        void addAgentTask(
                String p,
                String st,
                String t,
                String a
        ) {

            ContentValues v =
                    new ContentValues();

            v.put("pipeline_title", p);
            v.put("stage", st);
            v.put("task", t);
            v.put("agent", a);
            v.put("status", "Ожидает");
            v.put(
                    "created_at",
                    System.currentTimeMillis()
            );

            getWritableDatabase()
                    .insert(
                            "agent_tasks",
                            null,
                            v
                    );

            log(
                    "Создана задача агента: "
                    + t
            );
        }

        void updateAgentTaskStatus(
                String task,
                String status
        ) {

            ContentValues v =
                    new ContentValues();

            v.put("status", status);

            getWritableDatabase()
                    .update(
                            "agent_tasks",
                            v,
                            "task=?",
                            new String[]{task}
                    );
        }

        void addSection(
                String p,
                String t,
                String pg,
                String w,
                String a
        ) {

            ContentValues v =
                    new ContentValues();

            v.put("pipeline_title", p);
            v.put("section_title", t);
            v.put("pages", pg);
            v.put("words", w);
            v.put("agent", a);

            getWritableDatabase()
                    .insert(
                            "research_sections",
                            null,
                            v
                    );
        }

        void addCompetition(
                String t,
                String task,
                String p
        ) {

            ContentValues v =
                    new ContentValues();

            v.put("title", t);
            v.put("task", task);
            v.put("participants", p);
            v.put("status", "Подготовлено");
            v.put(
                    "created_at",
                    System.currentTimeMillis()
            );

            getWritableDatabase()
                    .insert(
                            "competitions",
                            null,
                            v
                    );
        }

        void addSource(
                String t,
                String u,
                String k
        ) {

            ContentValues v =
                    new ContentValues();

            v.put("title", t);
            v.put("url", u);
            v.put("kind", k);

            getWritableDatabase()
                    .insert(
                            "sources",
                            null,
                            v
                    );
        }
    }
}