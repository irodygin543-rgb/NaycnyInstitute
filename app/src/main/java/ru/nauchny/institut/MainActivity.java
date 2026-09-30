package ru.nauchny.institut;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MainActivity extends Activity {

    DB db;

    LinearLayout root, content;

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

    static final int PICK_DOCUMENT = 1001;

    // Локальный Qwen
    static final String LOCAL_QWEN_URL =
            "http://127.0.0.1:8080/v1/chat/completions";

    // Увеличенные таймауты:
    // загрузка модели и генерация на телефоне могут занимать время.
    static final int CONNECT_TIMEOUT_MS = 30000;
    static final int READ_TIMEOUT_MS = 120000;


    // =========================================================
    // МОДЕЛИ
    // =========================================================

    static class Department {
        String name, description;

        Department(String n, String d) {
            name = n;
            description = d;
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

        ResearchSection(
                String p,
                String t,
                String pg,
                String w,
                String a
        ) {
            pipeline = p;
            title = t;
            pages = pg;
            words = w;
            agent = a;
        }
    }

    static class Competition {
        String title, task, participants, status;

        Competition(
                String t,
                String ta,
                String p
        ) {
            title = t;
            task = ta;
            participants = p;
            status = "Подготовлено";
        }
    }

    static class AgentTask {
        String pipeline, stage, task, agent, status;

        AgentTask(
                String p,
                String s,
                String t,
                String a
        ) {
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
                    "Декомпозиция → подбор агентов → исследование → " +
                    "анализ документов → критик → рецензент → редактор";

            status = "Создано";
        }
    }

    static class Assignment {
        String title, type, status, departments, agents;

        Assignment(
                String t,
                String ty,
                String d,
                String a
        ) {
            title = t;
            type = ty;
            departments = d;
            agents = a;
            status = "Новое";
        }
    }

    static class Agent {
        String name, role, department;

        Agent(
                String n,
                String r,
                String d
        ) {
            name = n;
            role = r;
            department = d;
        }
    }


    // =========================================================
    // ANDROID
    // =========================================================

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        db = new DB(this);

        seed();

        showHome();
    }


    // =========================================================
    // НАЧАЛЬНЫЕ ДАННЫЕ
    // =========================================================

    void seed() {

        if (departments.size() == 0) {
            departments.add(
                    new Department(
                            "Кафедра искусственного интеллекта",
                            "Интеллектуальные системы и машинное обучение"
                    )
            );

            departments.add(
                    new Department(
                            "Кафедра информационной безопасности",
                            "Защита информации и цифровая криминалистика"
                    )
            );

            departments.add(
                    new Department(
                            "Кафедра радиотехники",
                            "Радиосистемы, электроника и обработка сигналов"
                    )
            );
        }

        if (agents.size() == 0) {

            agents.add(
                    new Agent(
                            "Александр Ньютон",
                            "Научный исследователь",
                            "Кафедра искусственного интеллекта"
                    )
            );

            agents.add(
                    new Agent(
                            "София Ковалевская",
                            "Методолог",
                            "Кафедра искусственного интеллекта"
                    )
            );

            agents.add(
                    new Agent(
                            "Иван Попов",
                            "Эксперт-рецензент",
                            "Кафедра информационной безопасности"
                    )
            );

            agents.add(
                    new Agent(
                            "Мария Соколова",
                            "Научный редактор",
                            "Кафедра радиотехники"
                    )
            );
        }
    }


    // =========================================================
    // UI
    // =========================================================

    TextView title(String s) {

        TextView v = new TextView(this);

        v.setText(s);
        v.setTextSize(25);
        v.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        v.setTextColor(0xff17324D);

        v.setPadding(
                0,
                12,
                0,
                18
        );

        return v;
    }


    Button nav(
            String text,
            final Runnable action
    ) {

        Button b = new Button(this);

        b.setText(text);
        b.setAllCaps(false);

        b.setOnClickListener(
                v -> action.run()
        );

        return b;
    }


    void base(String screen) {

        root = new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                22,
                18,
                22,
                18
        );

        setContentView(root);


        LinearLayout bar = new LinearLayout(this);

        bar.setOrientation(
                LinearLayout.HORIZONTAL
        );


        TextView brand = new TextView(this);

        brand.setText(
                "НАУЧНЫЙ ИНСТИТУТ"
        );

        brand.setTextSize(16);

        brand.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        brand.setTextColor(
                0xff17324D
        );


        bar.addView(
                brand,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );


        Button home = new Button(this);

        home.setText("Главная");

        home.setOnClickListener(
                v -> showHome()
        );

        bar.addView(home);

        root.addView(bar);


        content = new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );


        ScrollView sv = new ScrollView(this);

        sv.addView(content);


        root.addView(
                sv,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );
    }


    TextView card(
            String a,
            String b
    ) {

        TextView v = new TextView(this);

        v.setText(
                a + "\n" + b
        );

        v.setTextSize(16);

        v.setPadding(
                18,
                18,
                18,
                18
        );

        v.setBackgroundColor(
                0xffEAF0F5
        );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        p.setMargins(
                0,
                0,
                0,
                14
        );

        v.setLayoutParams(p);

        return v;
    }


    // =========================================================
    // ГЛАВНАЯ
    // =========================================================

    void showHome() {

        base("home");

        content.addView(
                title("Ректорат")
        );


        content.addView(
                card(
                        "Институт готов к расширению",
                        "Кафедры и агенты создаются динамически. Версия 0.1."
                )
        );


        content.addView(
                nav(
                        "🏛 Кафедры (" +
                                departments.size() +
                                ")",
                        this::showDepartments
                )
        );


        content.addView(
                nav(
                        "🤖 Научные агенты (" +
                                agents.size() +
                                ")",
                        this::showAgents
                )
        );


        content.addView(
                nav(
                        "🧠 Компетенции",
                        () -> info(
                                "Компетенции",
                                "Модель компетенций будет связана с агентами, кафедрами и научными проектами."
                        )
                )
        );


        content.addView(
                nav(
                        "🔬 Научные проекты",
                        () -> info(
                                "Научные проекты",
                                "Здесь появится конвейер: задача → исследование → эксперимент → статья → рецензирование."
                        )
                )
        );


        content.addView(
                nav(
                        "📚 Академия / повышение квалификации (" +
                                courses.size() +
                                ")",
                        this::showCourses
                )
        );


        content.addView(
                nav(
                        "📝 Статьи и редакция",
                        () -> info(
                                "Редакционно-издательский отдел",
                                "Будут версии рукописи, рецензии, библиография и нормативная проверка."
                        )
                )
        );


        content.addView(
                nav(
                        "🏛 Диссертационный совет",
                        () -> info(
                                "Диссертационный совет",
                                "Будут проекты диссертаций, отзывы, заседания, протоколы и решения."
                        )
                )
        );


        content.addView(
                nav(
                        "📎 Документы института (" +
                                documents.size() +
                                ")",
                        this::showDocuments
                )
        );


        content.addView(
                nav(
                        "🌐 Источники и интернет-исследования (" +
                                sources.size() +
                                ")",
                        this::showSources
                )
        );


        content.addView(
                nav(
                        "🔬 Web Research (" +
                                researchJobs.size() +
                                ")",
                        this::showResearch
                )
        );


        content.addView(
                nav(
                        "🤖 AI-оркестратор (" +
                                pipelineJobs.size() +
                                ")",
                        this::showPipelines
                )
        );


        content.addView(
                nav(
                        "🧠 Gemini: тест",
                        this::testGemini
                )
        );


        content.addView(
                nav(
                        "🏆 Соревнования агентов (" +
                                competitions.size() +
                                ")",
                        this::showCompetitions
                )
        );


        content.addView(
                nav(
                        "⚖ Нормативная база",
                        () -> info(
                                "Нормативная база",
                                "Отдельный обновляемый контур для законодательства, приказов и требований к научной и образовательной деятельности."
                        )
                )
        );


        // НОВОЕ:
        // настоящий тест локального Qwen
        content.addView(
                nav(
                        "🧠 Тест локального Qwen3 8B",
                        this::testLocalQwen
                )
        );


        content.addView(
                nav(
                        "📑 Научное производство (" +
                                assignments.size() +
                                ")",
                        this::showAssignments
                )
        );
    }


    // =========================================================
    // КАФЕДРЫ
    // =========================================================

    void showDepartments() {

        base("departments");

        content.addView(
                title("Кафедры")
        );

        for (Department d : departments) {
            content.addView(
                    card(
                            d.name,
                            d.description
                    )
            );
        }

        content.addView(
                nav(
                        "＋ Добавить кафедру",
                        this::addDepartment
                )
        );
    }


    void addDepartment() {

        final EditText name =
                new EditText(this);

        name.setHint(
                "Название кафедры"
        );


        final EditText desc =
                new EditText(this);

        desc.setHint(
                "Научное направление"
        );


        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                30,
                10,
                30,
                0
        );

        box.addView(name);
        box.addView(desc);


        new AlertDialog.Builder(this)
                .setTitle("Новая кафедра")
                .setView(box)
                .setPositiveButton(
                        "Создать",
                        (d, w) -> {

                            String n =
                                    name.getText()
                                            .toString()
                                            .trim();

                            if (!n.isEmpty()) {

                                String description =
                                        desc.getText()
                                                .toString();

                                departments.add(
                                        new Department(
                                                n,
                                                description
                                        )
                                );

                                db.addDepartment(
                                        n,
                                        description
                                );

                                showDepartments();
                            }
                        }
                )
                .setNegativeButton(
                        "Отмена",
                        null
                )
                .show();
    }


    // =========================================================
    // АГЕНТЫ
    // =========================================================

    void showAgents() {

        base("agents");

        content.addView(
                title("Научные агенты")
        );


        for (final Agent a : agents) {

            TextView av =
                    card(
                            a.name,
                            a.role +
                                    " • " +
                                    a.department
                    );

            av.setOnClickListener(
                    v -> showAgentProfile(a)
            );

            content.addView(av);
        }


        content.addView(
                nav(
                        "＋ Создать агента",
                        this::addAgent
                )
        );
    }


    void showAgentProfile(
            final Agent a
    ) {

        base("agent_profile");

        content.addView(
                title(
                        "Профиль научного агента"
                )
        );


        content.addView(
                card(
                        a.name,
                        a.role +
                                "\nКафедра: " +
                                a.department
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
                card(
                        "Обучение",
                        "Пройденные курсы будут отображаться здесь и влиять на профиль квалификации."
                )
        );


        content.addView(
                card(
                        "Научная деятельность",
                        "Поручения, статьи, НИР и результаты конкурсов будут связаны с профилем агента."
                )
        );


        content.addView(
                nav(
                        "✎ Изменить квалификацию и компетенции",
                        () -> editAgentProfile(a)
                )
        );


        content.addView(
                nav(
                        "📚 Назначить обучение",
                        this::showCourses
                )
        );
    }


    void editAgentProfile(
            final Agent a
    ) {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                30,
                0,
                30,
                0
        );


        EditText q =
                new EditText(this);

        q.setHint(
                "Квалификационный уровень"
        );

        q.setText(
                agentQualifications.get(a.name)
        );


        EditText c =
                new EditText(this);

        c.setHint(
                "Компетенции через запятую"
        );

        c.setText(
                agentCompetencies.get(a.name)
        );


        box.addView(q);
        box.addView(c);


        new AlertDialog.Builder(this)
                .setTitle(
                        "Профиль компетенций"
                )
                .setView(box)
                .setPositiveButton(
                        "Сохранить",
                        (d, w) -> {

                            String qualification =
                                    q.getText()
                                            .toString();

                            String competencies =
                                    c.getText()
                                            .toString();

                            agentQualifications.put(
                                    a.name,
                                    qualification
                            );

                            agentCompetencies.put(
                                    a.name,
                                    competencies
                            );

                            db.saveAgentProfile(
                                    a.name,
                                    qualification,
                                    competencies
                            );

                            showAgentProfile(a);
                        }
                )
                .setNegativeButton(
                        "Отмена",
                        null
                )
                .show();
    }


    void addAgent() {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                30,
                0,
                30,
                0
        );


        EditText n =
                new EditText(this);

        n.setHint(
                "Имя агента"
        );


        EditText r =
                new EditText(this);

        r.setHint(
                "Роль / специализация"
        );


        EditText dep =
                new EditText(this);

        dep.setHint(
                "Кафедра"
        );


        box.addView(n);
        box.addView(r);
        box.addView(dep);


        new AlertDialog.Builder(this)
                .setTitle(
                        "Новый научный агент"
                )
                .setView(box)
                .setPositiveButton(
                        "Создать",
                        (d, w) -> {

                            String name =
                                    n.getText()
                                            .toString()
                                            .trim();

                            if (!name.isEmpty()) {

                                String role =
                                        r.getText()
                                                .toString();

                                String department =
                                        dep.getText()
                                                .toString();

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
                .setNegativeButton(
                        "Отмена",
                        null
                )
                .show();
    }


    // =========================================================
    // НАУЧНОЕ ПРОИЗВОДСТВО
    // =========================================================

    void showAssignments() {

        base("assignments");

        content.addView(
                title("Научные поручения")
        );


        content.addView(
                card(
                        "Доказательная база",
                        "К научному поручению будут привязываться документы и интернет-источники."
                )
        );


        content.addView(
                card(
                        "Научное производство",
                        "Ректор ставит задачу, выбирает тип продукции и назначает кафедры/агентов."
                )
        );


        for (Assignment a : assignments) {

            content.addView(
                    card(
                            a.type +
                                    ": " +
                                    a.title,

                            "Статус: " +
                                    a.status +
                                    "\nКафедры: " +
                                    a.departments +
                                    "\nАгенты: " +
                                    a.agents
                    )
            );
        }


        content.addView(
                nav(
                        "＋ Новое научное поручение",
                        this::addAssignment
                )
        );
    }


    void addAssignment() {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                30,
                0,
                30,
                0
        );


        EditText title =
                new EditText(this);

        title.setHint(
                "Тема / название поручения"
        );


        EditText type =
                new EditText(this);

        type.setHint(
                "Тип: НИР, обзорная справка, статья, доклад..."
        );


        EditText dep =
                new EditText(this);

        dep.setHint(
                "Кафедры"
        );


        EditText ag =
                new EditText(this);

        ag.setHint(
                "Агенты или автоматически подобрать"
        );


        box.addView(title);
        box.addView(type);
        box.addView(dep);
        box.addView(ag);


        new AlertDialog.Builder(this)
                .setTitle(
                        "Новое научное поручение"
                )
                .setView(box)
                .setPositiveButton(
                        "Создать",
                        (d, w) -> {

                            String t =
                                    title.getText()
                                            .toString()
                                            .trim();

                            if (!t.isEmpty()) {

                                String ty =
                                        type.getText()
                                                .toString();

                                String departmentsText =
                                        dep.getText()
                                                .toString();

                                String agentsText =
                                        ag.getText()
                                                .toString();

                                assignments.add(
                                        new Assignment(
                                                t,
                                                ty,
                                                departmentsText,
                                                agentsText
                                        )
                                );

                                db.addAssignment(
                                        t,
                                        ty,
                                        departmentsText,
                                        agentsText
                                );

                                showAssignments();
                            }
                        }
                )
                .setNegativeButton(
                        "Отмена",
                        null
                )
                .show();
    }


    // =========================================================
    // АКАДЕМИЯ
    // =========================================================

    void showCourses() {

        base("courses");

        content.addView(
                title("Академия института")
        );


        content.addView(
                card(
                        "Повышение квалификации",
                        "Ректор добавляет учебные материалы, ссылки и документы."
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
                nav(
                        "＋ Создать курс / назначение",
                        this::addCourse
                )
        );
    }


    void addCourse() {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                30,
                0,
                30,
                0
        );


        EditText t =
                new EditText(this);

        t.setHint(
                "Название курса"
        );


        EditText m =
                new EditText(this);

        m.setHint(
                "Ссылка или название материала"
        );


        box.addView(t);
        box.addView(m);


        new AlertDialog.Builder(this)
                .setTitle(
                        "Повышение квалификации"
                )
                .setView(box)
                .setPositiveButton(
                        "Назначить",
                        (d, w) -> {

                            String title =
                                    t.getText()
                                            .toString()
                                            .trim();

                            if (!title.isEmpty()) {

                                String material =
                                        m.getText()
                                                .toString();

                                courses.add(
                                        new Course(
                                                title,
                                                material
                                        )
                                );

                                db.addCourse(
                                        title,
                                        material
                                );

                                showCourses();
                            }
                        }
                )
                .setNegativeButton(
                        "Отмена",
                        null
                )
                .show();
    }


    // =========================================================
    // ДОКУМЕНТЫ
    // =========================================================

    void showDocuments() {

        base("documents");

        content.addView(
                title("Документы института")
        );


        content.addView(
                card(
                        "Документальный контур",
                        "Выбирайте PDF, DOCX и другие файлы из памяти телефона."
                )
        );


        for (DocumentItem d : documents) {

            TextView v =
                    card(
                            d.name,
                            d.category +
                                    "\nСвязь: " +
                                    d.linkedTo +
                                    "\n" +
                                    d.uri
                    );

            v.setOnClickListener(
                    view -> showDocumentDetail(d)
            );

            content.addView(v);
        }


        content.addView(
                nav(
                        "🔎 Поиск по документам",
                        this::searchDocuments
                )
        );


        content.addView(
                nav(
                        "＋ Загрузить документ",
                        this::pickDocument
                )
        );
    }


    void searchDocuments() {

        final EditText q =
                new EditText(this);

        q.setHint(
                "Введите тему, термин или фразу"
        );


        new AlertDialog.Builder(this)
                .setTitle(
                        "Поиск по документам"
                )
                .setView(q)
                .setPositiveButton(
                        "Искать",
                        (d, w) -> {

                            String query =
                                    q.getText()
                                            .toString()
                                            .trim()
                                            .toLowerCase();

                            base("document_search");

                            content.addView(
                                    title(
                                            "Результаты поиска"
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


                            for (DocumentItem x : documents) {

                                if (
                                        x.name.toLowerCase()
                                                .contains(query)
                                                ||
                                        x.category.toLowerCase()
                                                .contains(query)
                                                ||
                                        x.linkedTo.toLowerCase()
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
                                                "Полнотекстовый и смысловой поиск по PDF/DOCX будет подключён следующим слоем."
                                        )
                                );
                            }
                        }
                )
                .setNegativeButton(
                        "Отмена",
                        null
                )
                .show();
    }


    void showDocumentDetail(
            final DocumentItem d
    ) {

        base("document_detail");

        content.addView(
                title(
                        "Карточка документа"
                )
        );


        content.addView(
                card(
                        d.name,
                        "URI: " + d.uri
                )
        );


        content.addView(
                card(
                        "Категория",
                        d.category
                )
        );


        content.addView(
                card(
                        "Связан с",
                        d.linkedTo
                )
        );


        content.addView(
                card(
                        "AI-обработка",
                        "Следующий слой: извлечение текста, разбиение на фрагменты, индексирование и передача релевантных фрагментов агентам."
                )
        );


        content.addView(
                nav(
                        "✎ Классифицировать и связать",
                        () -> editDocument(d)
                )
        );


        content.addView(
                nav(
                        "📖 Открыть файл",
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

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                30,
                0,
                30,
                0
        );


        EditText c =
                new EditText(this);

        c.setHint(
                "Категория"
        );

        c.setText(
                d.category
        );


        EditText l =
                new EditText(this);

        l.setHint(
                "Связь: агент / кафедра / курс / НИР"
        );

        l.setText(
                d.linkedTo
        );


        box.addView(c);
        box.addView(l);


        new AlertDialog.Builder(this)
                .setTitle(
                        "Классификация документа"
                )
                .setView(box)
                .setPositiveButton(
                        "Сохранить",
                        (x, w) -> {

                            d.category =
                                    c.getText()
                                            .toString();

                            d.linkedTo =
                                    l.getText()
                                            .toString();

                            db.updateDocument(
                                    d.name,
                                    d.category,
                                    d.linkedTo
                            );

                            showDocumentDetail(d);
                        }
                )
                .setNegativeButton(
                        "Отмена",
                        null
                )
                .show();
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
                requestCode == PICK_DOCUMENT
                        &&
                resultCode == RESULT_OK
                        &&
                data != null
                        &&
                data.getData() != null
        ) {

            Uri u =
                    data.getData();


            try {

                getContentResolver()
                        .takePersistableUriPermission(
                                u,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

            } catch (Exception ignored) {
            }


            String name =
                    u.getLastPathSegment();

            String uri =
                    u.toString();


            if (name == null) {
                name = "Документ";
            }


            documents.add(
                    new DocumentItem(
                            name,
                            uri,
                            "Не классифицирован",
                            "Не назначен"
                    )
            );


            db.addDocument(
                    name,
                    uri,
                    "Не классифицирован",
                    "Не назначен"
            );


            showDocuments();
        }
    }


    // =========================================================
    // КОМПЕТЕНЦИИ
    // =========================================================

    String suggestAgents(String task) {

        if (agents.size() == 0) {
            return "Нет зарегистрированных агентов";
        }


        String q =
                task.toLowerCase();


        StringBuilder out =
                new StringBuilder();


        for (Agent a : agents) {

            String c =
                    agentCompetencies.get(a.name);


            if (c != null && !c.isEmpty()) {

                String lc =
                        c.toLowerCase();


                if (
                        q.contains("норм")
                                &&
                        lc.contains("прав")
                ) {

                    if (out.length() > 0) {
                        out.append(", ");
                    }

                    out.append(a.name);

                } else if (
                        q.contains("дан")
                                &&
                        (
                                lc.contains("анал")
                                        ||
                                lc.contains("стат")
                        )
                ) {

                    if (out.length() > 0) {
                        out.append(", ");
                    }

                    out.append(a.name);

                } else if (
                        q.contains("радио")
                                &&
                        lc.contains("ради")
                ) {

                    if (out.length() > 0) {
                        out.append(", ");
                    }

                    out.append(a.name);
                }
            }
        }


        if (out.length() == 0) {
            out.append(agents.get(0).name);
        }


        return out.toString();
    }


    // =========================================================
    // СОРЕВНОВАНИЯ
    // =========================================================

    void showCompetitions() {

        base("competitions");

        content.addView(
                title("Соревнования агентов")
        );


        content.addView(
                card(
                        "Научное соревнование",
                        "Одинаковая задача может быть передана нескольким агентам. Результаты сравниваются и проходят рецензирование."
                )
        );


        for (Competition c : competitions) {

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
                nav(
                        "＋ Новое соревнование",
                        this::addCompetition
                )
        );
    }


    void addCompetition() {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                30,
                0,
                30,
                0
        );


        EditText t =
                new EditText(this);

        t.setHint(
                "Название соревнования"
        );


        EditText task =
                new EditText(this);

        task.setHint(
                "Одинаковая научная задача"
        );


        EditText p =
                new EditText(this);

        p.setHint(
                "Участники: агенты через запятую"
        );


        box.addView(t);
        box.addView(task);
        box.addView(p);


        new AlertDialog.Builder(this)
                .setTitle(
                        "Соревнование агентов"
                )
                .setView(box)
                .setPositiveButton(
                        "Создать",
                        (d, w) -> {

                            String title =
                                    t.getText()
                                            .toString()
                                            .trim();

                            if (!title.isEmpty()) {

                                String taskText =
                                        task.getText()
                                                .toString();

                                String participants =
                                        p.getText()
                                                .toString();

                                competitions.add(
                                        new Competition(
                                                title,
                                                taskText,
                                                participants
                                        )
                                );

                                db.addCompetition(
                                        title,
                                        taskText,
                                        participants
                                );

                                showCompetitions();
                            }
                        }
                )
                .setNegativeButton(
                        "Отмена",
                        null
                )
                .show();
    }


    // =========================================================
    // AI-ОРКЕСТРАТОР
    // =========================================================

    void showPipelines() {

        base("pipelines");

        content.addView(
                title("AI-оркестратор")
        );


        content.addView(
                card(
                        "Мультиагентный научный конвейер",
                        "Поручение ректора проходит последовательность этапов."
                )
        );


        for (final PipelineJob p : pipelineJobs) {

            TextView pv =
                    card(
                            p.title,
                            "Тип: " +
                                    p.type +
                                    "\nСтатус: " +
                                    p.status +
                                    "\nЭтапы: " +
                                    p.stages
                    );


            pv.setOnClickListener(
                    v -> showPipelineDetail(p)
            );


            content.addView(pv);
        }


        content.addView(
                nav(
                        "＋ Запустить научный конвейер",
                        this::addPipeline
                )
        );
    }


    void addTaskForStage(
            final PipelineJob p,
            final String stage
    ) {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                30,
                0,
                30,
                0
        );


        EditText task =
                new EditText(this);

        task.setHint(
                "Задача для агента"
        );


        EditText agent =
                new EditText(this);

        agent.setHint(
                "Агент или подобрать автоматически"
        );


        box.addView(task);
        box.addView(agent);


        box.addView(
                nav(
                        "🧠 Подобрать по компетенциям",
                        () ->
                                agent.setText(
                                        suggestAgents(
                                                task.getText()
                                                        .toString()
                                        )
                                )
                )
        );


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

                                agentTasks.add(
                                        new AgentTask(
                                                p.title,
                                                stage,
                                                t,
                                                a
                                        )
                                );

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
                .setNegativeButton(
                        "Отмена",
                        null
                )
                .show();
    }


    void showTasksForPipeline(
            final PipelineJob p
    ) {

        base("tasks");

        content.addView(
                title("Задачи агентов")
        );


        boolean any = false;


        for (final AgentTask t : agentTasks) {

            if (t.pipeline.equals(p.title)) {

                any = true;


                TextView tv =
                        card(
                                t.stage,
                                t.task +
                                        "\nАгент: " +
                                        t.agent +
                                        "\nСтатус: " +
                                        t.status
                        );


                tv.setOnClickListener(
                        v -> editAgentTask(t, p)
                );


                content.addView(tv);
            }
        }


        if (!any) {

            content.addView(
                    card(
                            "Задач пока нет",
                            "Нажмите на этап конвейера, чтобы создать задачу."
                    )
            );
        }
    }


    void editAgentTask(
            final AgentTask t,
            final PipelineJob p
    ) {

        String[] statuses = {
                "Ожидает",
                "В работе",
                "На проверке",
                "Доработка",
                "Принято",
                "Отклонено"
        };


        int selected =
                Math.max(
                        0,
                        Arrays.asList(
                                statuses
                        ).indexOf(
                                t.status
                        )
                );


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
                .setNegativeButton(
                        "Отмена",
                        null
                )
                .show();
    }


    void planPipeline(
            final PipelineJob p
    ) {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                30,
                0,
                30,
                0
        );


        EditText sections =
                new EditText(this);

        sections.setHint(
                "Количество основных разделов"
        );


        EditText pages =
                new EditText(this);

        pages.setHint(
                "Страниц на раздел"
        );


        EditText words =
                new EditText(this);

        words.setHint(
                "Ориентир слов на страницу"
        );


        box.addView(sections);
        box.addView(pages);
        box.addView(words);


        new AlertDialog.Builder(this)
                .setTitle(
                        "Планирование научного объёма"
                )
                .setView(box)
                .setPositiveButton(
                        "Сформировать",
                        (d, w) -> {

                            int n = 1;

                            try {
                                n = Integer.parseInt(
                                        sections.getText()
                                                .toString()
                                );
                            } catch (Exception ignored) {
                            }


                            int pg = 1;

                            try {
                                pg = Integer.parseInt(
                                        pages.getText()
                                                .toString()
                                );
                            } catch (Exception ignored) {
                            }


                            int wp = 300;

                            try {
                                wp = Integer.parseInt(
                                        words.getText()
                                                .toString()
                                );
                            } catch (Exception ignored) {
                            }


                            for (
                                    int i = 1;
                                    i <= n;
                                    i++
                            ) {

                                String sectionTitle =
                                        "Раздел " + i;


                                ResearchSection rs =
                                        new ResearchSection(
                                                p.title,
                                                sectionTitle,
                                                String.valueOf(pg),
                                                String.valueOf(
                                                        pg * wp
                                                ),
                                                "Подобрать автоматически"
                                        );


                                researchSections.add(rs);


                                db.addSection(
                                        p.title,
                                        sectionTitle,
                                        String.valueOf(pg),
                                        String.valueOf(pg * wp),
                                        "Подобрать автоматически"
                                );
                            }


                            showPlan(p);
                        }
                )
                .setNegativeButton(
                        "Отмена",
                        null
                )
                .show();
    }


    void showPlan(
            final PipelineJob p
    ) {

        base("research_plan");

        content.addView(
                title("План исследования")
        );


        int totalPages = 0;
        int totalWords = 0;
        int count = 0;


        for (ResearchSection r : researchSections) {

            if (r.pipeline.equals(p.title)) {

                count++;


                try {
                    totalPages +=
                            Integer.parseInt(r.pages);
                } catch (Exception ignored) {
                }


                try {
                    totalWords +=
                            Integer.parseInt(r.words);
                } catch (Exception ignored) {
                }


                content.addView(
                        card(
                                r.title,
                                "Объём: " +
                                        r.pages +
                                        " стр. / " +
                                        r.words +
                                        " слов\nАгент: " +
                                        r.agent
                        )
                );
            }
        }


        content.addView(
                card(
                        "Итого",
                        "Разделов: " +
                                count +
                                "\nПлановый объём: " +
                                totalPages +
                                " страниц\nОриентир: " +
                                totalWords +
                                " слов"
                )
        );


        content.addView(
                nav(
                        "＋ Добавить раздел",
                        () -> addSection(p)
                )
        );
    }


    void addSection(
            final PipelineJob p
    ) {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                30,
                0,
                30,
                0
        );


        EditText t =
                new EditText(this);

        t.setHint(
                "Название раздела"
        );


        EditText pg =
                new EditText(this);

        pg.setHint(
                "Страницы"
        );


        EditText w =
                new EditText(this);

        w.setHint(
                "Слова"
        );


        EditText a =
                new EditText(this);

        a.setHint(
                "Агент / подобрать автоматически"
        );


        box.addView(t);
        box.addView(pg);
        box.addView(w);
        box.addView(a);


        new AlertDialog.Builder(this)
                .setTitle(
                        "Раздел исследования"
                )
                .setView(box)
                .setPositiveButton(
                        "Добавить",
                        (d, x) -> {

                            String title =
                                    t.getText()
                                            .toString()
                                            .trim();

                            if (!title.isEmpty()) {

                                ResearchSection r =
                                        new ResearchSection(
                                                p.title,
                                                title,
                                                pg.getText()
                                                        .toString(),
                                                w.getText()
                                                        .toString(),
                                                a.getText()
                                                        .toString()
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
                .setNegativeButton(
                        "Отмена",
                        null
                )
                .show();
    }


    void showPipelineDetail(
            final PipelineJob p
    ) {

        base("pipeline_detail");

        content.addView(
                title("Научный конвейер")
        );


        content.addView(
                card(
                        p.title,
                        "Тип продукции: " +
                                p.type +
                                "\nОбщий статус: " +
                                p.status
                )
        );


        content.addView(
                card(
                        "Техническое задание ректора",
                        "Объём: " +
                                p.pages +
                                " страниц\n" +
                                "Требования: " +
                                p.standard +
                                "\nСрок: " +
                                p.deadline +
                                "\nОбласть: " +
                                p.field +
                                "\nWeb Research: " +
                                p.webResearch +
                                "\nРецензирование: " +
                                p.reviewers
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


        for (final String st : stages) {

            TextView sv =
                    card(
                            st,
                            "Статус этапа: ожидает выполнения\nРезультат: пока отсутствует"
                    );


            sv.setOnClickListener(
                    v -> addTaskForStage(
                            p,
                            st
                    )
            );


            content.addView(sv);
        }


        content.addView(
                nav(
                        "📐 План объёма и структуры",
                        () -> planPipeline(p)
                )
        );


        content.addView(
                nav(
                        "📋 Задачи агентов",
                        () -> showTasksForPipeline(p)
                )
        );


        content.addView(
                nav(
                        "▶ Перевести в работу",
                        () -> {

                            p.status =
                                    "В работе";

                            db.updatePipelineStatus(
                                    p.title,
                                    p.status
                            );

                            showPipelineDetail(p);
                        }
                )
        );


        content.addView(
                nav(
                        "⏸ Приостановить",
                        () -> {

                            p.status =
                                    "Приостановлено";

                            db.updatePipelineStatus(
                                    p.title,
                                    p.status
                            );

                            showPipelineDetail(p);
                        }
                )
        );


        content.addView(
                nav(
                        "✓ Отметить как завершённый",
                        () -> {

                            p.status =
                                    "Завершено";

                            db.updatePipelineStatus(
                                    p.title,
                                    p.status
                            );

                            showPipelineDetail(p);
                        }
                )
        );
    }


    void addPipeline() {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                30,
                0,
                30,
                0
        );


        EditText title =
                new EditText(this);

        title.setHint(
                "Название исследования / поручения"
        );


        Spinner type =
                new Spinner(this);


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
                new EditText(this);

        pages.setHint(
                "Требуемый объём, страниц"
        );


        EditText standard =
                new EditText(this);

        standard.setHint(
                "Требования: ВАК / ГОСТ / журнал / организация"
        );


        EditText deadline =
                new EditText(this);

        deadline.setHint(
                "Срок выполнения"
        );


        EditText field =
                new EditText(this);

        field.setHint(
                "Научная область / кафедра"
        );


        EditText web =
                new EditText(this);

        web.setHint(
                "Web Research: да/нет"
        );


        EditText reviewers =
                new EditText(this);

        reviewers.setHint(
                "Критика/рецензирование: да/нет; число рецензентов"
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
                .setTitle(
                        "Ректорское научное поручение"
                )
                .setView(box)
                .setPositiveButton(
                        "Создать конвейер",
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
                                                pages.getText()
                                                        .toString(),
                                                standard.getText()
                                                        .toString(),
                                                deadline.getText()
                                                        .toString(),
                                                field.getText()
                                                        .toString(),
                                                web.getText()
                                                        .toString(),
                                                reviewers.getText()
                                                        .toString()
                                        );


                                pipelineJobs.add(p);


                                db.addPipeline(
                                        t,
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
                .setNegativeButton(
                        "Отмена",
                        null
                )
                .show();
    }


    // =========================================================
    // WEB RESEARCH
    // =========================================================

    void showResearch() {

        base("research");

        content.addView(
                title("Web Research")
        );


        content.addView(
                card(
                        "Исследовательский режим",
                        "Ректор задаёт тему и область поиска. В серверном/локальном AI-слое агент сможет работать с веб-источниками."
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
                nav(
                        "＋ Новое интернет-исследование",
                        this::addResearch
                )
        );
    }


    void addResearch() {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                30,
                0,
                30,
                0
        );


        EditText t =
                new EditText(this);

        t.setHint(
                "Тема исследования"
        );


        EditText scope =
                new EditText(this);

        scope.setHint(
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

                                String s =
                                        scope.getText()
                                                .toString();


                                researchJobs.add(
                                        new ResearchJob(
                                                topic,
                                                s
                                        )
                                );


                                db.addResearch(
                                        topic,
                                        s
                                );


                                showResearch();
                            }
                        }
                )
                .setNegativeButton(
                        "Отмена",
                        null
                )
                .show();
    }


    // =========================================================
    // ИСТОЧНИКИ
    // =========================================================

    void showSources() {

        base("sources");

        content.addView(
                title("Источники и интернет")
        );


        content.addView(
                card(
                        "Web Research",
                        "Агент сможет искать интернет-источники, сохранять библиографию и связывать утверждения с источниками."
                )
        );


        for (Source x : sources) {

            content.addView(
                    card(
                            x.title,
                            x.kind +
                                    "\n" +
                                    x.url
                    )
            );
        }


        content.addView(
                nav(
                        "＋ Добавить источник",
                        this::addSource
                )
        );
    }


    void addSource() {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                30,
                0,
                30,
                0
        );


        EditText t =
                new EditText(this);

        t.setHint(
                "Название источника"
        );


        EditText u =
                new EditText(this);

        u.setHint(
                "URL или имя файла"
        );


        EditText k =
                new EditText(this);

        k.setHint(
                "Тип: интернет / PDF / DOCX / нормативный документ"
        );


        box.addView(t);
        box.addView(u);
        box.addView(k);


        new AlertDialog.Builder(this)
                .setTitle(
                        "Источник знаний"
                )
                .setView(box)
                .setPositiveButton(
                        "Сохранить",
                        (d, w) -> {

                            String title =
                                    t.getText()
                                            .toString()
                                            .trim();


                            if (!title.isEmpty()) {

                                String url =
                                        u.getText()
                                                .toString();

                                String kind =
                                        k.getText()
                                                .toString();


                                sources.add(
                                        new Source(
                                                title,
                                                url,
                                                kind
                                        )
                                );


                                db.addSource(
                                        title,
                                        url,
                                        kind
                                );


                                showSources();
                            }
                        }
                )
                .setNegativeButton(
                        "Отмена",
                        null
                )
                .show();
    }


    // =========================================================
    // ЛОКАЛЬНЫЙ QWEN3 8B
    // =========================================================

    void testLocalQwen() {

        final EditText input =
                new EditText(this);

        input.setHint(
                "Например: что такое искусственный интеллект?"
        );


        new AlertDialog.Builder(this)
                .setTitle(
                        "Тест локального Qwen3 8B"
                )
                .setView(input)
                .setPositiveButton(
                        "Отправить",
                        (dialog, which) -> {

                            String prompt =
                                    input.getText()
                                            .toString()
                                            .trim();


                            if (prompt.isEmpty()) {

                                info(
                                        "Qwen",
                                        "Введите вопрос."
                                );

                                return;
                            }


                            final ProgressDialog pd =
                                    new ProgressDialog(this);

                            pd.setMessage(
                                    "Локальный Qwen думает...\n" +
                                    "На телефоне генерация может занимать до нескольких минут."
                            );

                            pd.setCancelable(false);

                            pd.show();


                            new Thread(() -> {

                                final String result =
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


    String callLocalQwen(
            String prompt
    ) {

        HttpURLConnection conn = null;


        try {

            URL url =
                    new URL(
                            LOCAL_QWEN_URL
                    );


            conn =
                    (HttpURLConnection)
                            url.openConnection();


            conn.setRequestMethod(
                    "POST"
            );


            conn.setConnectTimeout(
                    CONNECT_TIMEOUT_MS
            );


            conn.setReadTimeout(
                    READ_TIMEOUT_MS
            );


            conn.setDoOutput(true);

            conn.setDoInput(true);


            conn.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=utf-8"
            );


            conn.setRequestProperty(
                    "Accept",
                    "application/json"
            );


            String safePrompt =
                    jsonEscape(prompt);


            String json =
                    "{"
                            + "\"model\":\"Qwen3-8B-Q4_K_M.gguf\","
                            + "\"messages\":["
                            + "{"
                            + "\"role\":\"user\","
                            + "\"content\":\""
                            + safePrompt
                            + "\""
                            + "}"
                            + "],"
                            + "\"max_tokens\":256,"
                            + "\"temperature\":0.2,"
                            + "\"chat_template_kwargs\":"
                            + "{\"enable_thinking\":false}"
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

                return "Ошибка HTTP " +
                        code +
                        ":\n\n" +
                        response;
            }


            String text =
                    extractJsonString(
                            response,
                            "\"content\":"
                    );


            if (
                    text != null
                            &&
                    !text.trim().isEmpty()
            ) {

                return text;
            }


            return response;

        } catch (
                java.net.SocketTimeoutException e
        ) {

            return
                    "Локальный Qwen отвечает слишком долго.\n\n" +
                    "Это НЕ означает, что модель сломана.\n\n" +
                    "Увеличен таймаут приложения до 120 секунд.\n" +
                    "Если модель всё ещё генерирует ответ, повторите тест с коротким вопросом.";

        } catch (
                java.net.ConnectException e
        ) {

            return
                    "Не удалось подключиться к Qwen.\n\n" +
                    "Проверьте, что llama-server запущен в Termux на:\n" +
                    "127.0.0.1:8080";

        } catch (Exception e) {

            return
                    "Ошибка локального Qwen:\n\n" +
                    e.toString();

        } finally {

            if (conn != null) {
                conn.disconnect();
            }
        }
    }


    String readStream(
            InputStream stream
    ) throws Exception {

        if (stream == null) {
            return "";
        }


        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                stream,
                                "UTF-8"
                        )
                );


        StringBuilder out =
                new StringBuilder();


        String line;


        while (
                (line = reader.readLine())
                        != null
        ) {

            out.append(line);
        }


        reader.close();


        return out.toString();
    }


    String jsonEscape(
            String s
    ) {

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


    String extractJsonString(
            String json,
            String marker
    ) {

        int index =
                json.indexOf(marker);


        if (index < 0) {
            return null;
        }


        int start =
                index +
                        marker.length();


        while (
                start < json.length()
                        &&
                Character.isWhitespace(
                        json.charAt(start)
                )
        ) {

            start++;
        }


        if (
                start >= json.length()
                        ||
                json.charAt(start) != '"'
        ) {

            return null;
        }


        start++;


        StringBuilder out =
                new StringBuilder();


        boolean escaped = false;


        for (
                int i = start;
                i < json.length();
                i++
        ) {

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

                    case '/':
                        out.append('/');
                        break;

                    default:
                        out.append(ch);
                        break;
                }


                escaped = false;

            } else if (ch == '\\') {

                escaped = true;

            } else if (ch == '"') {

                return out.toString();

            } else {

                out.append(ch);
            }
        }


        return null;
    }


    // =========================================================
    // GEMINI
    // =========================================================

    void testGemini() {

        final EditText input =
                new EditText(this);

        input.setHint(
                "Введите запрос для Gemini..."
        );


        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                20,
                0,
                20,
                0
        );


        box.addView(input);


        new AlertDialog.Builder(this)
                .setTitle(
                        "Тест Gemini API"
                )
                .setMessage(
                        "Ключ Gemini не хранится в коде. " +
                        "Введите новый ключ, который вы создали после отзыва старого."
                )
                .setView(box)
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


                            askGeminiKey(prompt);
                        }
                )
                .setNegativeButton(
                        "Отмена",
                        null
                )
                .show();
    }


    void askGeminiKey(
            final String prompt
    ) {

        final EditText key =
                new EditText(this);

        key.setHint(
                "AIza... / новый ключ Gemini"
        );

        key.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        );


        new AlertDialog.Builder(this)
                .setTitle(
                        "Новый ключ Gemini"
                )
                .setView(key)
                .setPositiveButton(
                        "Отправить",
                        (d, w) -> {

                            String apiKey =
                                    key.getText()
                                            .toString()
                                            .trim();


                            if (apiKey.isEmpty()) {

                                info(
                                        "Gemini",
                                        "Ключ не введён."
                                );

                                return;
                            }


                            ProgressDialog pd =
                                    new ProgressDialog(this);

                            pd.setMessage(
                                    "Gemini отвечает..."
                            );

                            pd.setCancelable(false);

                            pd.show();


                            new Thread(() -> {

                                String result =
                                        callGemini(
                                                prompt,
                                                apiKey
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
                            +
                            model
                            +
                            ":generateContent";


            URL url =
                    new URL(urlString);


            conn =
                    (HttpURLConnection)
                            url.openConnection();


            conn.setRequestMethod(
                    "POST"
            );


            conn.setConnectTimeout(
                    30000
            );


            conn.setReadTimeout(
                    120000
            );


            conn.setRequestProperty(
                    "Content-Type",
                    "application/json"
            );


            conn.setRequestProperty(
                    "X-goog-api-key",
                    apiKey
            );


            conn.setDoOutput(true);


            String safePrompt =
                    jsonEscape(prompt);


            String json =
                    "{\"contents\":[{\"parts\":[{\"text\":\""
                            +
                            safePrompt
                            +
                            "\"}]}]}";


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
                stream = conn.getInputStream();
            } else {
                stream = conn.getErrorStream();
            }


            String raw =
                    readStream(stream);


            if (code < 200 || code >= 300) {

                return
                        "Ошибка HTTP " +
                        code +
                        ":\n\n" +
                        raw;
            }


            String result =
                    extractJsonString(
                            raw,
                            "\"text\":"
                    );


            if (
                    result != null
                            &&
                    !result.isEmpty()
            ) {

                return result;
            }


            return raw;

        } catch (Exception e) {

            return
                    "Ошибка Gemini:\n\n" +
                    e.toString();

        } finally {

            if (conn != null) {
                conn.disconnect();
            }
        }
    }


    // =========================================================
    // INFO
    // =========================================================

    void info(
            String h,
            String t
    ) {

        new AlertDialog.Builder(this)
                .setTitle(h)
                .setMessage(t)
                .setPositiveButton(
                        "Понятно",
                        null
                )
                .show();
    }


    // =========================================================
    // SQLITE
    // =========================================================

    static class DB extends SQLiteOpenHelper {

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
                    "CREATE TABLE pipeline_stages(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "pipeline_title TEXT," +
                    "stage TEXT," +
                    "status TEXT," +
                    "result TEXT)"
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

            if (oldV < 2) {

                d.execSQL(
                        "CREATE TABLE IF NOT EXISTS agent_profiles(" +
                        "agent_name TEXT PRIMARY KEY," +
                        "qualification TEXT," +
                        "competencies TEXT)"
                );
            }


            if (oldV < 3) {

                d.execSQL(
                        "CREATE TABLE IF NOT EXISTS documents(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT," +
                        "uri TEXT," +
                        "category TEXT," +
                        "linked_to TEXT," +
                        "created_at INTEGER)"
                );
            }


            if (oldV < 4) {

                d.execSQL(
                        "CREATE TABLE IF NOT EXISTS research_jobs(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "topic TEXT," +
                        "scope TEXT," +
                        "status TEXT," +
                        "created_at INTEGER)"
                );
            }


            if (oldV < 5) {

                d.execSQL(
                        "CREATE TABLE IF NOT EXISTS pipeline_jobs(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT," +
                        "type TEXT," +
                        "status TEXT," +
                        "created_at INTEGER)"
                );
            }


            if (oldV < 6) {

                d.execSQL(
                        "CREATE TABLE IF NOT EXISTS pipeline_stages(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "pipeline_title TEXT," +
                        "stage TEXT," +
                        "status TEXT," +
                        "result TEXT)"
                );
            }


            if (oldV < 7) {

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
            }


            if (oldV < 8) {

                d.execSQL(
                        "CREATE TABLE IF NOT EXISTS competitions(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT," +
                        "task TEXT," +
                        "participants TEXT," +
                        "status TEXT," +
                        "created_at INTEGER)"
                );
            }


            if (oldV < 9) {

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
            }


            if (oldV < 10) {

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


            log(
                    "Создана кафедра: " + n
            );
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


            log(
                    "Создан агент: " + n
            );
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
                    "Создано научное поручение: " + t
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


            log(
                    "Создан курс: " + t
            );
        }


        void saveAgentProfile(
                String n,
                String q,
                String c
        ) {

            ContentValues v =
                    new ContentValues();

            v.put(
                    "agent_name",
                    n
            );

            v.put(
                    "qualification",
                    q
            );

            v.put(
                    "competencies",
                    c
            );


            getWritableDatabase()
                    .insertWithOnConflict(
                            "agent_profiles",
                            null,
                            v,
                            SQLiteDatabase.CONFLICT_REPLACE
                    );


            log(
                    "Изменён профиль агента: " + n
            );
        }


        void updateDocument(
                String n,
                String c,
                String l
        ) {

            ContentValues v =
                    new ContentValues();

            v.put(
                    "category",
                    c
            );

            v.put(
                    "linked_to",
                    l
            );


            getWritableDatabase()
                    .update(
                            "documents",
                            v,
                            "name=?",
                            new String[]{n}
                    );


            log(
                    "Изменён документ: " + n
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

            v.put(
                    "pipeline_title",
                    p
            );

            v.put(
                    "section_title",
                    t
            );

            v.put(
                    "pages",
                    pg
            );

            v.put(
                    "words",
                    w
            );

            v.put(
                    "agent",
                    a
            );


            getWritableDatabase()
                    .insert(
                            "research_sections",
                            null,
                            v
                    );


            log(
                    "Добавлен раздел НИР: " + t
            );
        }


        void addCompetition(
                String t,
                String task,
                String p
        ) {

            ContentValues v =
                    new ContentValues();

            v.put(
                    "title",
                    t
            );

            v.put(
                    "task",
                    task
            );

            v.put(
                    "participants",
                    p
            );

            v.put(
                    "status",
                    "Подготовлено"
            );

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


            log(
                    "Создано соревнование агентов: " + t
            );
        }


        void updateAgentTaskStatus(
                String task,
                String st
        ) {

            ContentValues v =
                    new ContentValues();

            v.put(
                    "status",
                    st
            );


            getWritableDatabase()
                    .update(
                            "agent_tasks",
                            v,
                            "task=?",
                            new String[]{task}
                    );


            log(
                    "Изменён статус задачи агента: " +
                            task +
                            " -> " +
                            st
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

            v.put(
                    "pipeline_title",
                    p
            );

            v.put(
                    "stage",
                    st
            );

            v.put(
                    "task",
                    t
            );

            v.put(
                    "agent",
                    a
            );

            v.put(
                    "status",
                    "Ожидает"
            );

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
                    "Создана задача агента: " + t
            );
        }


        void updatePipelineStatus(
                String t,
                String st
        ) {

            ContentValues v =
                    new ContentValues();

            v.put(
                    "status",
                    st
            );


            getWritableDatabase()
                    .update(
                            "pipeline_jobs",
                            v,
                            "title=?",
                            new String[]{t}
                    );


            log(
                    "Изменён статус конвейера: " +
                            t +
                            " -> " +
                            st
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

            v.put(
                    "title",
                    t
            );

            v.put(
                    "type",
                    ty
            );

            v.put(
                    "status",
                    "Создано"
            );

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

            x.put(
                    "pipeline_title",
                    t
            );

            x.put(
                    "type",
                    ty
            );

            x.put(
                    "pages",
                    pg
            );

            x.put(
                    "standard",
                    st
            );

            x.put(
                    "deadline",
                    dl
            );

            x.put(
                    "field",
                    f
            );

            x.put(
                    "web_research",
                    wr
            );

            x.put(
                    "reviewers",
                    rv
            );


            getWritableDatabase()
                    .insertWithOnConflict(
                            "pipeline_specs",
                            null,
                            x,
                            SQLiteDatabase.CONFLICT_REPLACE
                    );


            log(
                    "Создано ректорское научное поручение: " +
                            t
            );
        }


        void addResearch(
                String t,
                String sc
        ) {

            ContentValues v =
                    new ContentValues();

            v.put(
                    "topic",
                    t
            );

            v.put(
                    "scope",
                    sc
            );

            v.put(
                    "status",
                    "Подготовлено"
            );

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
                    "Создано Web Research: " + t
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

            v.put(
                    "name",
                    n
            );

            v.put(
                    "uri",
                    u
            );

            v.put(
                    "category",
                    c
            );

            v.put(
                    "linked_to",
                    l
            );

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


            log(
                    "Загружен документ: " + n
            );
        }


        void addSource(
                String t,
                String u,
                String k
        ) {

            ContentValues v =
                    new ContentValues();

            v.put(
                    "title",
                    t
            );

            v.put(
                    "url",
                    u
            );

            v.put(
                    "kind",
                    k
            );


            getWritableDatabase()
                    .insert(
                            "sources",
                            null,
                            v
                    );


            log(
                    "Добавлен источник: " + t
            );
        }
    }
}