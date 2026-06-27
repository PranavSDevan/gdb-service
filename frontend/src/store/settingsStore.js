import { create } from 'zustand';
import { settingsService } from '../services/settingsService';
import { useThemeStore } from './themeStore';

const translations = {
  en: {
    dashboard: "Dashboard",
    accounts: "Accounts",
    transactions: "Transactions",
    users: "Users",
    reports: "Reports",
    settings: "Settings",
    profile: "My Profile",
    signOut: "Sign Out",
    welcome: "Welcome back",
    totalBalance: "Total Balance",
    totalAccounts: "Total Accounts",
    todayTransactions: "Today's Transactions",
    recentTransactions: "Recent Transactions",
    quickActions: "Quick Actions",
    deposit: "Deposit",
    withdraw: "Withdraw",
    transfer: "Transfer",
    transferLimits: "Transfer Limits",
    allAccounts: "All Accounts",
    createSavings: "Create Savings",
    createCurrent: "Create Current",
    systemStatus: "System Status",
    online: "Online",
    offline: "Offline",
    myProfile: "My Profile",
    allTransactions: "All Transactions",
    allUsers: "All Users",
    createUser: "Create User",
    creditCards: "Credit Cards",
    collapse: "Collapse",
    welcome_subtitle: "Here's what's happening with your bank today.",
    welcomeSubtitle: "Here's what's happening with your bank today.",
    statements: "Bank Statements",
    bankStatements: "Bank Statements",
    cardDashboard: "Card Dashboard",
    cardDetails: "Card Details",
    cardTransactions: "Card Transactions",
    payBill: "Pay Bill",
    applyCreditCard: "Apply Credit Card",
    simulatePurchase: "Simulate Purchase",
    creditLimit: "Credit Limit",
    availableCredit: "Available Credit",
    outstandingAmount: "Outstanding Amount",
    minimumDue: "Minimum Due",
    nextDueDate: "Next Due Date",
    paymentDueSoon: "Payment Due Soon",
    accountSummary: "Account Summary",
    recentActivity: "Recent Activity",
  },
  hi: {
    dashboard: "डैशबोर्ड",
    accounts: "खाते",
    transactions: "लेन-देन",
    users: "उपयोगकर्ता",
    reports: "रिपोर्ट",
    settings: "सेटिंग्स",
    profile: "मेरी प्रोफ़ाइल",
    signOut: "साइन आउट",
    welcome: "वापसी पर स्वागत है",
    totalBalance: "कुल शेष",
    totalAccounts: "कुल खाते",
    todayTransactions: "आज के लेन-देन",
    recentTransactions: "हाल के लेन-देन",
    quickActions: "त्वरित कार्रवाई",
    deposit: "जमा करें",
    withdraw: "निकासी",
    transfer: "स्थानांतरण",
    transferLimits: "स्थानांतरण सीमाएं",
    allAccounts: "सभी खाते",
    createSavings: "बचत खाता खोलें",
    createCurrent: "चालू खाता खोलें",
    systemStatus: "सिस्टम की स्थिति",
    online: "ऑनलाइन",
    offline: "ऑफ़लाइन",
    myProfile: "मेरी प्रोफ़ाइल",
    allTransactions: "सभी लेन-देन",
    allUsers: "सभी उपयोगकर्ता",
    createUser: "उपयोगकर्ता बनाएँ",
    creditCards: "क्रेडिट कार्ड",
    collapse: "छोटा करें",
    welcome_subtitle: "यहाँ आज आपके बैंक की जानकारी है।",
    welcomeSubtitle: "यहाँ आज आपके बैंक की जानकारी है।",
    statements: "बैंक स्टेटमेंट",
    bankStatements: "बैंक स्टेटमेंट",
    cardDashboard: "कार्ड डैशबोर्ड",
    cardDetails: "कार्ड विवरण",
    cardTransactions: "कार्ड लेन-देन",
    payBill: "बिल भुगतान",
    applyCreditCard: "क्रेडिट कार्ड आवेदन",
    simulatePurchase: "खरीद अनुकरण",
    creditLimit: "क्रेडिट सीमा",
    availableCredit: "उपलब्ध क्रेडिट",
    outstandingAmount: "बकाया राशि",
    minimumDue: "न्यूनतम देय",
    nextDueDate: "अगली देय तिथि",
    paymentDueSoon: "भुगतान जल्द देय",
    accountSummary: "खाता सारांश",
    recentActivity: "हालिया गतिविधि",
  },
  ta: {
    dashboard: "டாஷ்போர்டு",
    accounts: "கணக்குகள்",
    transactions: "பரிவர்த்தனைகள்",
    users: "பயனர்கள்",
    reports: "அறிக்கைகள்",
    settings: "அமைப்புகள்",
    profile: "எனது சுயவிவரம்",
    signOut: "வெளியேறு",
    welcome: "மீண்டும் வருக",
    totalBalance: "மொத்த இருப்பு",
    totalAccounts: "மொத்த கணக்குகள்",
    todayTransactions: "இன்றைய பரிவர்த்தனைகள்",
    recentTransactions: "சமீபத்திய பரிவர்த்தனைகள்",
    quickActions: "விரைவான செயல்கள்",
    deposit: "டெபாசிட்",
    withdraw: "பணம் எடுத்தல்",
    transfer: "பரிமாற்றம்",
    transferLimits: "பரிமாற்ற வரம்புகள்",
    allAccounts: "அனைத்து கணக்குகள்",
    createSavings: "சேமிப்பு கணக்கு உருவாக்கு",
    createCurrent: "நடப்பு கணக்கு உருவாக்கு",
    systemStatus: "கணினி நிலை",
    online: "ஆன்லைன்",
    offline: "ஆஃப்லைன்",
    myProfile: "எனது சுயவிவரம்",
    allTransactions: "அனைத்து பரிவர்த்தனைகள்",
    allUsers: "அனைத்து பயனர்கள்",
    createUser: "பயனரை உருவாக்கு",
    creditCards: "கிரெடிட் கார்டுகள்",
    collapse: "சுருக்கு",
    welcome_subtitle: "இன்று உங்கள் வங்கியின் நிலை இதோ.",
    welcomeSubtitle: "இன்று உங்கள் வங்கியின் நிலை இதோ.",
    statements: "வங்கி அறிக்கை",
    bankStatements: "வங்கி அறிக்கை",
    cardDashboard: "கார்டு டாஷ்போர்டு",
    cardDetails: "கார்டு விவரங்கள்",
    cardTransactions: "கார்டு பரிவர்த்தனைகள்",
    payBill: "பில் செலுத்து",
    applyCreditCard: "கிரெடிட் கார்டு விண்ணப்பம்",
    simulatePurchase: "கொள்முதல் உருவகம்",
    creditLimit: "கடன் வரம்பு",
    availableCredit: "கிடைக்கும் கடன்",
    outstandingAmount: "நிலுவை தொகை",
    minimumDue: "குறைந்தபட்ச தொகை",
    nextDueDate: "அடுத்த தவணை தேதி",
    paymentDueSoon: "விரைவில் பணம் செலுத்த வேண்டும்",
    accountSummary: "கணக்கு சுருக்கம்",
    recentActivity: "சமீபத்திய செயல்பாடு",
  },
  te: {
    dashboard: "డ్యాష్‌బోర్డ్",
    accounts: "ఖాతాలు",
    transactions: "లావాదేవీలు",
    users: "వినియోగదారులు",
    reports: "నివేదికలు",
    settings: "సెట్టింగులు",
    profile: "నా ప్రొఫైల్",
    signOut: "సైన్ అవుట్",
    welcome: "తిరిగి స్వాగతం",
    totalBalance: "మొత్తం బ్యాలెన్స్",
    totalAccounts: "మొత్తం ఖాతాలు",
    todayTransactions: "నేటి లావాదేవీలు",
    recentTransactions: "ఇటీవలి లావాదేవీలు",
    quickActions: "త్వరిత చర్యలు",
    deposit: "డిపాజిట్",
    withdraw: "విత్‌డ్రా",
    transfer: "బదిలీ",
    transferLimits: "బదిలీ పరిమితులు",
    allAccounts: "అన్ని ఖాతాలు",
    createSavings: "పొదుపు ఖాతా తెరువు",
    createCurrent: "కరెంట్ ఖాతా తెరువు",
    systemStatus: "సిస్టమ్ స్థితి",
    online: "ఆన్‌లైన్",
    offline: "ఆఫ్‌లైన్",
    myProfile: "నా ప్రొఫైల్",
    allTransactions: "అన్ని లావాదేవీలు",
    allUsers: "ఆన్ని వినియోగదారులు",
    createUser: "వినియోగదారుని సృష్టించు",
    creditCards: "క్రెడిట్ కార్డ్‌లు",
    collapse: "కుదించు",
    welcome_subtitle: "ఈరోజు మీ బ్యాంక్ వివరాలు ఇక్కడ ఉన్నాయి.",
    welcomeSubtitle: "ఈరోజు మీ బ్యాంక్ వివరాలు ఇక్కడ ఉన్నాయి.",
    statements: "బ్యాంక్ స్టేట్‌మెంట్",
    bankStatements: "బ్యాంక్ స్టేట్‌మెంట్",
    cardDashboard: "కార్డ్ డ్యాష్‌బోర్డ్",
    cardDetails: "కార్డ్ వివరాలు",
    cardTransactions: "కార్డ్ లావాదేవీలు",
    payBill: "బిల్లు చెల్లించు",
    applyCreditCard: "క్రెడిట్ కార్డ్ దరఖాస్తు",
    simulatePurchase: "కొనుగోలు అనుకరణ",
    creditLimit: "క్రెడిట్ పరిమితి",
    availableCredit: "అందుబాటులో ఉన్న క్రెడిట్",
    outstandingAmount: "బకాయి మొత్తం",
    minimumDue: "కనిష్ట చెల్లింపు",
    nextDueDate: "తదుపరి గడువు తేదీ",
    paymentDueSoon: "త్వరలో చెల్లింపు",
    accountSummary: "ఖాతా సారాంశం",
    recentActivity: "ఇటీవలి కార్యకలాపం",
  }
};

export const useSettingsStore = create((set, get) => ({
  language: 'en',
  dateFormat: 'DD/MM/YYYY',
  currency: 'INR',
  timezone: 'Asia/Kolkata',

  setLanguage: (language) => set({ language }),
  setDateFormat: (dateFormat) => set({ dateFormat }),
  setCurrency: (currency) => set({ currency }),
  setTimezone: (timezone) => set({ timezone }),

  fetchAndApplySettings: async (userId) => {
    try {
      const data = await settingsService.getSettings(userId);
      if (data) {
        set({
          language: data.language || 'en',
          dateFormat: data.dateFormat || 'DD/MM/YYYY',
          currency: data.currency || 'INR',
        });

        // Sync with ThemeStore
        const themeStore = useThemeStore.getState();
        const loadedTheme = (data.theme || 'LIGHT').toLowerCase();
        const loadedCompact = data.compactMode !== undefined ? data.compactMode : false;
        const loadedSidebar = data.sidebarCollapsed !== undefined ? data.sidebarCollapsed : false;

        themeStore.setTheme(loadedTheme);
        themeStore.setCompactMode(loadedCompact);
        themeStore.setSidebarCollapsed(loadedSidebar);
      }
    } catch (error) {
      console.error('Failed to fetch and apply backend settings:', error);
    }
  },

  t: (key) => {
    const { language } = get();
    const langDict = translations[language] || translations['en'];
    return langDict[key] || translations['en'][key] || key;
  },

  formatDateString: (dateValue) => {
    if (!dateValue) return 'N/A';
    try {
      let dateStr = String(dateValue);
      if (!dateStr.endsWith('Z') && !dateStr.includes('+') && !dateStr.includes('-', 10)) {
        dateStr = dateStr.replace(' ', 'T') + 'Z';
      }
      const date = new Date(dateStr);
      if (isNaN(date.getTime())) return 'N/A';

      const formatType = get().dateFormat;
      
      const dd = String(date.getDate()).padStart(2, '0');
      const mm = String(date.getMonth() + 1).padStart(2, '0');
      const yyyy = date.getFullYear();

      let timeStr = date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

      if (formatType === 'MM/DD/YYYY') {
        return `${mm}/${dd}/${yyyy} • ${timeStr}`;
      } else if (formatType === 'YYYY-MM-DD') {
        return `${yyyy}-${mm}-${dd} • ${timeStr}`;
      } else {
        return `${dd}/${mm}/${yyyy} • ${timeStr}`;
      }
    } catch {
      return 'N/A';
    }
  },

  formatCurrencyAmount: (amount) => {
    const { currency } = get();
    
    // Exchange rates from INR
    const exchangeRates = {
      INR: 1,
      USD: 1 / 83.5,
      EUR: 1 / 91.0,
      GBP: 1 / 106.0,
    };

    const rate = exchangeRates[currency] || 1;
    const convertedAmount = (amount || 0) * rate;

    let locale = 'en-IN';
    if (currency === 'USD') locale = 'en-US';
    else if (currency === 'EUR') locale = 'de-DE';
    else if (currency === 'GBP') locale = 'en-GB';

    return new Intl.NumberFormat(locale, {
      style: 'currency',
      currency: currency,
      maximumFractionDigits: currency === 'INR' ? 0 : 2,
    }).format(convertedAmount);
  }
}));

export default useSettingsStore;
