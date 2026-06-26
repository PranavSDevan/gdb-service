/**
 * AI Assistant Service
 * 
 * Local client-side processing service for GDB Copilot.
 * Maps user queries to answers and automated route transitions.
 */

import { aiApi } from './apiConfig';
import { useAuthStore } from '../store/authStore';

const USE_REAL_API = true;
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

const INTENT_ROUTES = [
  {
    keywords: ['card', 'credit card', 'credit-card', 'apply card', 'apply for card', 'pay bill', 'outstanding'],
    route: '/credit-cards',
    response: 'Sure! I am redirecting you to the Credit Cards dashboard. You can apply for a new card (Silver, Gold, or Platinum) or pay your outstanding statement balance there.'
  },
  {
    keywords: ['statement', 'bank statement', 'pdf', 'csv', 'download statement', 'history', 'statements'],
    route: '/statements',
    response: 'Certainly. I am opening the Bank Statements manager. You can select your account, specify a date range, view daily transaction trends, and download detailed reports.'
  },
  {
    keywords: ['setting', 'settings', 'theme', 'password', 'change password', 'profile', 'notification', 'security', 'language'],
    route: '/settings',
    response: 'Of course! I will navigate you to your Account Settings page to update language, switch color theme (Light/Dark/System), and configure notification toggles.'
  },
  {
    keywords: ['send', 'pay', 'transfer', 'deposit', 'withdraw', 'money', 'transaction', 'transactions'],
    route: '/transactions',
    response: 'Understood. Navigating you to the Transaction Hub where you can perform deposits, withdrawals, and domestic/international fund transfers.'
  },
  {
    keywords: ['dashboard', 'home', 'main', 'summary', 'overview'],
    route: '/dashboard',
    response: 'Navigating back to the main Account Overview dashboard to view your savings and current accounts.'
  }
];

const FAQ_RESPONSES = [
  {
    keywords: ['what is gdb', 'about gdb', 'global digital bank'],
    response: 'Global Digital Bank (GDB) is a modern, secure microservices-based online banking platform. We offer instant savings accounts, multi-tier credit card programs, comprehensive transaction logging, and online bank statement generations.'
  },
  {
    keywords: ['how to apply', 'card application', 'credit card apply'],
    response: 'To apply for a credit card, click the "Apply Card" chip or type "card". You will be routed to the Credit Card section. Select your salary bracket to apply for a Silver, Gold, or Platinum card instantly!'
  },
  {
    keywords: ['interest rate', 'savings interest', 'interest rates'],
    response: 'GDB offers competitive interest rates: 4.5% p.a. on Savings accounts, and up to 7.0% p.a. on fixed-term investment accounts. Check with branch managers for current promotion details.'
  },
  {
    keywords: ['features', 'what can you do', 'capabilities', 'help'],
    response: 'I am the GDB AI Copilot! I can answer general banking questions and quickly navigate you across the platform. You can say things like:\n- "Take me to my credit cards"\n- "I want to download statements"\n- "Open settings"\n- "Show transaction history"'
  },
  {
    keywords: ['hello', 'hi', 'hey', 'greetings', 'who are you', 'copilot'],
    response: 'Hello! I am your GDB Copilot, your virtual banking assistant. Feel free to ask me to navigate to different pages or answer any GDB product questions!'
  }
];

export const aiAssistantService = {
  processQuery: async (queryText) => {
    if (USE_REAL_API) {
      try {
        const userId = useAuthStore.getState().user?.id || 'admin';
        const response = await aiApi.post('/api/v1/ai/chat', {
          query: queryText,
          userId: userId
        });
        return response.data; // Expected: { text: "...", route: "..." }
      } catch (error) {
        console.warn("Failed fetching AI response from microservice, falling back to local NLP:", error);
      }
    }

    // Local query matcher fallback
    await delay(700); // Simulate thinking delay
    const normalized = queryText.toLowerCase().trim();

    if (!normalized) {
      return {
        text: 'How can I assist you today? Feel free to ask me to open a page or answer bank questions.'
      };
    }

    // 1. Check intent routes (navigation)
    for (const rule of INTENT_ROUTES) {
      if (rule.keywords.some(keyword => normalized.includes(keyword))) {
        return {
          text: rule.response,
          route: rule.route
        };
      }
    }

    // 2. Check general FAQs
    for (const faq of FAQ_RESPONSES) {
      if (faq.keywords.some(keyword => normalized.includes(keyword))) {
        return {
          text: faq.response
        };
      }
    }

    // 3. Fallback answer
    return {
      text: `I'm not completely sure how to help with "${queryText}". However, I can help you navigate to features. Try clicking one of the quick suggestion chips below or ask me to "open settings" or "show credit cards".`
    };
  }
};
