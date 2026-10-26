import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/** Student Opportunity Radar — standalone Java Swing desktop application. */
public class StudentOpportunityRadar extends JFrame {
    private static final Color INK = new Color(24, 35, 52), MUTED = new Color(105, 118, 137), BG = new Color(246, 248, 251), WHITE = Color.WHITE, BLUE = new Color(54, 105, 230), LINE = new Color(228, 233, 240), GREEN = new Color(38, 142, 105);
    private static final String[] CATEGORIES = {"All opportunities", "Internships", "Hackathons", "Workshops", "Competitions", "Scholarships", "Placements"};
    private final List<Opportunity> all = new ArrayList<>();
    private final Set<String> saved = new HashSet<>();
    private final Map<String,String> statuses = new HashMap<>();
    private final JPanel cards = new JPanel(new GridLayout(0, 2, 16, 16));
    private final JTextField search = new JTextField();
    private final JComboBox<String> category = new JComboBox<>(CATEGORIES);
    private final JCheckBox eligibleOnly = new JCheckBox("Only show matches for me");
    private final JLabel countLabel = new JLabel();
    private String page = "Explore";
    private static final Path DATA = Paths.get(System.getProperty("user.home"), ".student-opportunity-radar.properties");

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new StudentOpportunityRadar().setVisible(true)); }
    StudentOpportunityRadar() {
        super("Student Opportunity Radar"); setDefaultCloseOperation(EXIT_ON_CLOSE); setMinimumSize(new Dimension(1060, 720)); setSize(1240, 820); setLocationRelativeTo(null);
        load(); seed(); getContentPane().setBackground(BG); setLayout(new BorderLayout()); add(sidebar(), BorderLayout.WEST); add(mainPanel(), BorderLayout.CENTER); refresh();
    }
    private JPanel sidebar() {
        JPanel side = new JPanel(); side.setBackground(INK); side.setPreferredSize(new Dimension(236, 0)); side.setBorder(new EmptyBorder(24,18,22,16)); side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        JLabel brand = new JLabel("✦  Opportunity"); brand.setForeground(WHITE); brand.setFont(new Font("SansSerif",Font.BOLD,19)); side.add(brand);
        JLabel radar = new JLabel("     RADAR"); radar.setForeground(new Color(151,178,255)); radar.setFont(new Font("SansSerif",Font.BOLD,11)); radar.setBorder(new EmptyBorder(2,0,35,0)); side.add(radar);
        JLabel menu = new JLabel("DISCOVER"); menu.setForeground(new Color(145,158,178)); menu.setFont(new Font("SansSerif",Font.BOLD,10)); menu.setBorder(new EmptyBorder(0,10,10,0)); side.add(menu);
        for(String item : new String[]{"Explore", "Saved opportunities", "My applications"}) {
            JButton b = new JButton((item.equals("Explore") ? "⌕   " : item.startsWith("Saved") ? "♡   " : "▤   ") + item); b.setHorizontalAlignment(SwingConstants.LEFT); b.setForeground(WHITE); b.setFont(new Font("SansSerif",Font.PLAIN,14)); b.setFocusPainted(false); b.setBorder(new EmptyBorder(12,12,12,8)); b.setMaximumSize(new Dimension(210,45)); b.setBackground(item.equals(page)?new Color(53,70,98):INK); b.setOpaque(true); b.addActionListener(e->{page=item; for(Component c:side.getComponents()) if(c instanceof JButton) c.setBackground(((JButton)c).getText().endsWith(item)?new Color(53,70,98):INK); refresh();}); side.add(b); side.add(Box.createVerticalStrut(5));
        }
        side.add(Box.createVerticalGlue()); JPanel profile = new JPanel(new BorderLayout(10,0)); profile.setOpaque(false); JLabel avatar = new JLabel("S", SwingConstants.CENTER); avatar.setOpaque(true); avatar.setBackground(new Color(86,111,158)); avatar.setForeground(WHITE); avatar.setFont(new Font("SansSerif",Font.BOLD,15)); avatar.setPreferredSize(new Dimension(36,36)); JLabel who = new JLabel("Student profile"); who.setForeground(WHITE); who.setFont(new Font("SansSerif",Font.BOLD,12)); profile.add(avatar,BorderLayout.WEST); profile.add(who,BorderLayout.CENTER); side.add(profile); return side;
    }
    private JPanel mainPanel() {
        JPanel main = new JPanel(new BorderLayout()); main.setBackground(BG); main.setBorder(new EmptyBorder(28,34,26,34));
        JPanel top = new JPanel(new BorderLayout(15,15)); top.setOpaque(false); JPanel greeting = new JPanel(); greeting.setOpaque(false); greeting.setLayout(new BoxLayout(greeting,BoxLayout.Y_AXIS)); JLabel title = new JLabel("Find your next opportunity"); title.setFont(new Font("SansSerif",Font.BOLD,27)); title.setForeground(INK); JLabel sub = new JLabel("A smarter way to discover what’s next in your career."); sub.setFont(new Font("SansSerif",Font.PLAIN,14)); sub.setForeground(MUTED); sub.setBorder(new EmptyBorder(7,0,0,0)); greeting.add(title); greeting.add(sub); top.add(greeting,BorderLayout.NORTH);
        JPanel tools = new JPanel(new BorderLayout(12,0)); tools.setOpaque(false); search.setFont(new Font("SansSerif",Font.PLAIN,14)); search.putClientProperty("JTextField.placeholderText","Search roles, skills, companies..."); search.setPreferredSize(new Dimension(300,42)); search.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),new EmptyBorder(8,12,8,12))); category.setFont(new Font("SansSerif",Font.PLAIN,13)); category.setPreferredSize(new Dimension(175,42)); category.setBackground(WHITE); eligibleOnly.setOpaque(false); eligibleOnly.setForeground(MUTED); eligibleOnly.setFont(new Font("SansSerif",Font.PLAIN,12)); tools.add(search,BorderLayout.CENTER); tools.add(category,BorderLayout.EAST); tools.add(eligibleOnly,BorderLayout.SOUTH); top.add(tools,BorderLayout.CENTER);
        search.getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){refresh();} public void removeUpdate(DocumentEvent e){refresh();} public void changedUpdate(DocumentEvent e){refresh();}}); category.addActionListener(e->refresh()); eligibleOnly.addActionListener(e->refresh());
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false); header.add(top,BorderLayout.CENTER); countLabel.setForeground(MUTED); countLabel.setFont(new Font("SansSerif",Font.PLAIN,12)); countLabel.setBorder(new EmptyBorder(18,0,10,0)); header.add(countLabel,BorderLayout.SOUTH); main.add(header,BorderLayout.NORTH);
        cards.setOpaque(false); JScrollPane scroll=new JScrollPane(cards); scroll.setBorder(null); scroll.getViewport().setOpaque(false); scroll.setOpaque(false); scroll.getVerticalScrollBar().setUnitIncrement(18); main.add(scroll,BorderLayout.CENTER); return main;
    }
    private void refresh() {
        if(cards==null)return; cards.removeAll(); String q=search.getText().trim().toLowerCase(); String cat=(String)category.getSelectedItem(); List<Opportunity> shown=new ArrayList<>();
        for(Opportunity o:all){ if(page.equals("Saved opportunities")&&!saved.contains(o.id))continue; if(page.equals("My applications")&&!statuses.containsKey(o.id))continue; if(!page.equals("Saved opportunities")&&!page.equals("My applications")&&!cat.equals("All opportunities")&&!o.category.equals(cat))continue; if(eligibleOnly.isSelected()&&!o.eligible)continue; String hay=(o.title+" "+o.org+" "+o.category+" "+o.skills+" "+o.location).toLowerCase(); if(!hay.contains(q))continue; shown.add(o); }
        countLabel.setText(page.equals("Explore")?shown.size()+" opportunities to explore":"Showing "+shown.size()+" "+page.toLowerCase());
        for(Opportunity o:shown) cards.add(card(o)); if(shown.isEmpty()){JPanel empty=new JPanel(new GridBagLayout());empty.setOpaque(false);JLabel e=new JLabel("No opportunities here yet. Try changing your filters.");e.setForeground(MUTED);empty.add(e);cards.setLayout(new BorderLayout());cards.add(empty,BorderLayout.CENTER);} else cards.setLayout(new GridLayout(0,2,16,16)); cards.revalidate();cards.repaint();
    }
    private JPanel card(Opportunity o){
        JPanel p=new JPanel(new BorderLayout(0,12));p.setBackground(WHITE);p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),new EmptyBorder(18,18,16,18)));
        JPanel head=new JPanel(new BorderLayout(10,0));head.setOpaque(false);JPanel icon=new JPanel(new GridBagLayout());icon.setPreferredSize(new Dimension(42,42));icon.setBackground(o.tint);JLabel il=new JLabel(o.category.equals("Internships")?"↗":o.category.equals("Hackathons")?"⌘":o.category.equals("Scholarships")?"✦":"◎");il.setForeground(o.color);il.setFont(new Font("SansSerif",Font.BOLD,20));icon.add(il);JPanel labels=new JPanel();labels.setOpaque(false);labels.setLayout(new BoxLayout(labels,BoxLayout.Y_AXIS));JLabel org=new JLabel(o.org);org.setForeground(MUTED);org.setFont(new Font("SansSerif",Font.PLAIN,12));JLabel name=new JLabel(o.title);name.setForeground(INK);name.setFont(new Font("SansSerif",Font.BOLD,16));name.setBorder(new EmptyBorder(4,0,0,0));labels.add(org);labels.add(name);JButton save=new JButton(saved.contains(o.id)?"♥":"♡");save.setToolTipText("Save opportunity");save.setFont(new Font("SansSerif",Font.PLAIN,21));save.setForeground(saved.contains(o.id)?new Color(220,78,96):MUTED);save.setBorderPainted(false);save.setContentAreaFilled(false);save.addActionListener(e->{if(!saved.add(o.id))saved.remove(o.id);persist();refresh();});head.add(icon,BorderLayout.WEST);head.add(labels,BorderLayout.CENTER);head.add(save,BorderLayout.EAST);p.add(head,BorderLayout.NORTH);
        JLabel detail=new JLabel("◷  "+o.type+"     ·     ◉  "+o.location);detail.setForeground(MUTED);detail.setFont(new Font("SansSerif",Font.PLAIN,12));p.add(detail,BorderLayout.CENTER);
        JPanel bottom=new JPanel(new BorderLayout(0,10));bottom.setOpaque(false);JPanel meta=new JPanel(new BorderLayout());meta.setOpaque(false);JLabel deadline=new JLabel("Deadline  "+o.deadline);deadline.setForeground(new Color(173,102,34));deadline.setFont(new Font("SansSerif",Font.BOLD,12));JLabel match=new JLabel(o.eligible?"✓  Good match":"Open to all");match.setForeground(o.eligible?GREEN:MUTED);match.setFont(new Font("SansSerif",Font.BOLD,11));meta.add(deadline,BorderLayout.WEST);meta.add(match,BorderLayout.EAST);bottom.add(meta,BorderLayout.NORTH);
        JPanel actions=new JPanel(new BorderLayout(8,0));actions.setOpaque(false);JLabel status=new JLabel(statuses.getOrDefault(o.id,"Not applied"));status.setForeground(MUTED);status.setFont(new Font("SansSerif",Font.PLAIN,11));JButton apply=new JButton(statuses.containsKey(o.id)?"Update status":"Track application");apply.setFont(new Font("SansSerif",Font.BOLD,12));apply.setForeground(BLUE);apply.setBackground(new Color(239,244,255));apply.setFocusPainted(false);apply.setBorder(new EmptyBorder(9,12,9,12));apply.addActionListener(e->changeStatus(o));actions.add(status,BorderLayout.CENTER);actions.add(apply,BorderLayout.EAST);bottom.add(actions,BorderLayout.SOUTH);p.add(bottom,BorderLayout.SOUTH);return p;
    }
    private void changeStatus(Opportunity o){String[] opts={"Interested","Applied","Interviewing","Offer received","Not applied"};String current=statuses.getOrDefault(o.id,opts[0]);String next=(String)JOptionPane.showInputDialog(this,"Update your application progress:",o.title,JOptionPane.PLAIN_MESSAGE,null,opts,current);if(next!=null){if(next.equals("Not applied"))statuses.remove(o.id);else statuses.put(o.id,next);persist();refresh();}}
    private void seed(){String[] rows={
        "intern-1|Software Engineering Intern|Google|Internships|Paid · 12 weeks|Bengaluru / Hybrid|Nov 12, 2026|Java, Python, Data Structures|1",
        "intern-2|Product Design Intern|Adobe|Internships|Paid · 10 weeks|Noida / Hybrid|Nov 18, 2026|Figma, UX Research, Prototyping|0",
        "hack-1|Build for Tomorrow|Devfolio|Hackathons|₹2,00,000 prize pool|Online|Nov 08, 2026|AI, Web Development, Java|1",
        "work-1|Cloud & AI Bootcamp|AWS Student Community|Workshops|Free · Certificate|Online|Nov 15, 2026|Cloud, Python, AI|1",
        "comp-1|Smart India Hackathon|Government of India|Competitions|National · Team event|India|Nov 20, 2026|Java, IoT, Problem Solving|1",
        "sch-1|HDFC Bank Parivartan Scholarship|HDFC Bank|Scholarships|Up to ₹75,000|India|Dec 05, 2026|Undergraduate, Financial need|1",
        "place-1|Graduate Engineer Trainee|Tata Consultancy Services|Placements|Full-time · 6–9 LPA|India|Nov 28, 2026|Java, SQL, Communication|1",
        "intern-3|Data Analyst Intern|Flipkart|Internships|Paid · 6 months|Bengaluru|Nov 10, 2026|SQL, Python, Analytics|0",
        "hack-2|ClimateTech Innovation Sprint|Unstop|Hackathons|₹1,00,000 prize pool|Online|Nov 22, 2026|Sustainability, Web Development|0",
        "work-2|Resume & Interview Masterclass|NASSCOM FutureSkills|Workshops|Free · Live session|Online|Nov 06, 2026|Communication, Interview Prep|1"};for(String r:rows){String[] a=r.split("\\|");all.add(new Opportunity(a));}}
    private void load(){Properties p=new Properties();try(InputStream in=Files.newInputStream(DATA)){p.load(in);for(String s:p.getProperty("saved","").split(","))if(!s.isBlank())saved.add(s);for(String e:p.stringPropertyNames())if(e.startsWith("status."))statuses.put(e.substring(7),p.getProperty(e));}catch(IOException ignored){}}
    private void persist(){Properties p=new Properties();p.setProperty("saved",String.join(",",saved));statuses.forEach((k,v)->p.setProperty("status."+k,v));try{Files.createDirectories(DATA.getParent());try(OutputStream out=Files.newOutputStream(DATA)){p.store(out,"Student Opportunity Radar data");}}catch(IOException e){JOptionPane.showMessageDialog(this,"Could not save your changes: "+e.getMessage());}}
    private static class Opportunity{String id,title,org,category,type,location,deadline,skills;boolean eligible;Color tint=new Color(236,242,255),color=BLUE;Opportunity(String[]a){id=a[0];title=a[1];org=a[2];category=a[3];type=a[4];location=a[5];deadline=a[6];skills=a[7];eligible=a[8].equals("1");}}
}
