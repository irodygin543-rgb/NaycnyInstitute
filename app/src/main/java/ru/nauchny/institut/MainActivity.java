package ru.nauchny.institut;

import android.app.*;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.os.Bundle;
import android.content.*;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class MainActivity extends Activity {

    DB db;
    LinearLayout root, content;

    ArrayList<Department> departments = new ArrayList<>();
    ArrayList<Agent> agents = new ArrayList<>();
    ArrayList<Assignment> assignments = new ArrayList<>();
    ArrayList<Course> courses = new ArrayList<>();
    ArrayList<Source> sources = new ArrayList<>();

    HashMap<String, String> agentQualifications = new HashMap<>();
    HashMap<String, String> agentCompetencies = new HashMap<>();

    ArrayList<DocumentItem> documents = new ArrayList<>();
    ArrayList<ResearchJob> researchJobs = new ArrayList<>();
    ArrayList<PipelineJob> pipelineJobs = new ArrayList<>();
    ArrayList<AgentTask> agentTasks = new ArrayList<>();
    ArrayList<Competition> competitions = new ArrayList<>();
    ArrayList<ResearchSection> researchSections = new ArrayList<>();

    static final int PICK_DOCUMENT = 1001;

    // =========================================================
    // ЛОКАЛЬНЫЙ QWEN3
    // =========================================================

    static final String LOCAL_QWEN_URL =
            "http://127.0.0.1:8080/v1/chat/completions";

    static final String LOCAL_QWEN_MODEL =
            "Qwen3-8B-Q4_K_M.gguf";

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

        DocumentItem(String n, String