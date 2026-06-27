import { creditCardsApi } from '../../../services/apiConfig';
import { useAuthStore } from '../../../store/authStore';

const USE_REAL_API = true;

// Initial default card (mock fallback)
let mockCards = [
  {
    id: 'CARD_1001',
    cardNumber: '**** **** **** 4589',
    cardType: 'Platinum',
    creditLimit: 500000,
    availableCredit: 125000,
    outstandingAmount: 375000,
    minimumDue: 18750,
    nextDueDate: new Date(Date.now() + 5 * 24 * 60 * 60 * 1000).toISOString(),
    status: 'Active',
    cardHolderName: 'John Doe'
  }
];

// Generate some mock transactions (mock fallback)
const generateMockTransactions = () => {
  const types = ['Purchase', 'Payment', 'Refund'];
  const merchants = ['Amazon', 'Flipkart', 'Swiggy', 'Zomato', 'Uber', 'Bill Payment', 'Refund - Myntra'];
  const statuses = ['Completed', 'Pending'];
  
  const transactions = [];
  let currentDate = new Date();
  
  for (let i = 0; i < 45; i++) {
    const type = types[Math.floor(Math.random() * types.length)];
    const merchant = merchants[Math.floor(Math.random() * merchants.length)];
    const amount = Math.floor(Math.random() * 10000) + 100;
    const status = statuses[Math.floor(Math.random() * statuses.length)];
    
    currentDate = new Date(currentDate.getTime() - Math.floor(Math.random() * 5 + 1) * 24 * 60 * 60 * 1000);
    
    transactions.push({
      id: `TXN${10000 + i}`,
      cardId: 'CARD_1001', // Link to default card
      date: currentDate.toISOString(),
      merchant: type === 'Payment' ? 'Credit Card Bill Payment' : merchant,
      amount: amount,
      type: type,
      status: status
    });
  }
  return transactions;
};

let mockTransactions = generateMockTransactions();

// Simulate network delay for mock fallback
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

export const creditCardService = {
  // Get all cards for the user
  getAllCards: async () => {
    if (USE_REAL_API) {
      try {
        const userId = useAuthStore.getState().user?.id || 'admin';
        const response = await creditCardsApi.get(`/api/v1/credit-cards/user/${userId}`);
        // If the backend returns empty, we can auto-seed or show empty.
        // Let's return the real backend cards.
        return response.data;
      } catch (error) {
        console.warn("Failed fetching credit cards from backend, falling back to mock:", error);
      }
    }

    await delay(600);
    return mockCards;
  },

  // Get specific card or default to first active
  getDashboardData: async (cardId = null) => {
    if (USE_REAL_API) {
      try {
        if (cardId) {
          const response = await creditCardsApi.get(`/api/v1/credit-cards/${cardId}`);
          return response.data;
        } else {
          const userId = useAuthStore.getState().user?.id || 'admin';
          const response = await creditCardsApi.get(`/api/v1/credit-cards/user/${userId}`);
          const cards = response.data;
          return cards.length > 0 ? cards[0] : null;
        }
      } catch (error) {
        console.warn("Failed fetching card dashboard data from backend, falling back to mock:", error);
      }
    }

    await delay(800);
    if (mockCards.length === 0) return null;
    if (cardId) {
      return mockCards.find(c => c.id === cardId) || mockCards[0];
    }
    return mockCards[0];
  },
  
  applyForCard: async (applicationData) => {
    if (USE_REAL_API) {
      try {
        const userId = useAuthStore.getState().user?.id || 'admin';
        const fullName = useAuthStore.getState().user?.full_name || 'Cardholder';
        const payload = {
          userId,
          cardType: applicationData.cardType,
          cardHolderName: fullName
        };
        const response = await creditCardsApi.post(`/api/v1/credit-cards/apply`, payload);
        return { 
          success: true, 
          message: "Application submitted successfully", 
          applicationId: response.data.id 
        };
      } catch (error) {
        console.error("Failed submitting credit card application to backend:", error);
        throw new Error(error.response?.data?.message || "Failed to apply for card");
      }
    }

    await delay(1500);
    if (!applicationData.employmentType || !applicationData.salary || !applicationData.cardType) {
      throw new Error("Missing required fields");
    }

    const limits = {
      'Silver': 100000,
      'Gold': 250000,
      'Platinum': 500000
    };

    const newLimit = limits[applicationData.cardType] || 100000;
    const newCard = {
      id: `CARD_${Math.floor(Math.random() * 10000)}`,
      cardNumber: `**** **** **** ${Math.floor(1000 + Math.random() * 9000)}`,
      cardType: applicationData.cardType,
      creditLimit: newLimit,
      availableCredit: newLimit,
      outstandingAmount: 0,
      minimumDue: 0,
      nextDueDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString(),
      status: 'Active',
      cardHolderName: useAuthStore.getState().user?.full_name || 'Cardholder'
    };

    mockCards.push(newCard);

    return { 
      success: true, 
      message: "Application submitted successfully", 
      applicationId: "APP" + Math.floor(Math.random() * 100000) 
    };
  },

  getTransactions: async (filters, cardId = null) => {
    let txns = [];
    let isMockFallback = false;

    if (USE_REAL_API) {
      try {
        if (!cardId) {
          const userId = useAuthStore.getState().user?.id || 'admin';
          const response = await creditCardsApi.get(`/api/v1/credit-cards/user/${userId}`);
          const cards = response.data;
          if (cards.length === 0) return [];
          cardId = cards[0].id;
        }
        const response = await creditCardsApi.get(`/api/v1/credit-cards/${cardId}/transactions`);
        txns = response.data;
      } catch (error) {
        console.warn("Failed fetching credit card transactions, falling back to mock:", error);
        isMockFallback = true;
      }
    } else {
      isMockFallback = true;
    }

    if (isMockFallback) {
      await delay(800);
      const targetCardId = cardId || mockCards[0]?.id;
      txns = mockTransactions.filter(t => t.cardId === targetCardId);
    }
    
    let filtered = [...txns];
    if (filters) {
      if (filters.type && filters.type !== 'All') {
        filtered = filtered.filter(t => t.type === filters.type);
      }
      if (filters.fromDate) {
        filtered = filtered.filter(t => new Date(t.date) >= new Date(filters.fromDate));
      }
      if (filters.toDate) {
        const toDateEnd = new Date(filters.toDate);
        toDateEnd.setDate(toDateEnd.getDate() + 1);
        filtered = filtered.filter(t => new Date(t.date) < toDateEnd);
      }
    }
    return filtered;
  },

  payBill: async (paymentData, cardId = null) => {
    if (USE_REAL_API) {
      try {
        if (!cardId) {
          const userId = useAuthStore.getState().user?.id || 'admin';
          const response = await creditCardsApi.get(`/api/v1/credit-cards/user/${userId}`);
          const cards = response.data;
          if (cards.length === 0) throw new Error("No active card found");
          cardId = cards[0].id;
        }
        const response = await creditCardsApi.post(`/api/v1/credit-cards/${cardId}/pay`, {
          amount: paymentData.amount
        });
        return { success: true, transactionId: response.data.id || `PAY${Math.floor(Math.random() * 100000)}` };
      } catch (error) {
        console.error("Failed paying credit card bill:", error);
        throw new Error(error.response?.data?.message || "Failed to pay credit card bill");
      }
    }

    await delay(1200);
    
    const targetCardId = cardId || mockCards[0]?.id;
    const cardIndex = mockCards.findIndex(c => c.id === targetCardId);
    
    if (cardIndex === -1) throw new Error("Card not found");
    const card = mockCards[cardIndex];

    if (!paymentData.amount || paymentData.amount <= 0) {
      throw new Error("Invalid payment amount");
    }
    if (!paymentData.debitAccount) {
      throw new Error("Debit account is required");
    }
    if (paymentData.amount > card.outstandingAmount) {
      throw new Error("Payment cannot exceed outstanding amount");
    }

    card.outstandingAmount -= paymentData.amount;
    card.availableCredit += paymentData.amount;
    if (card.outstandingAmount === 0) {
      card.minimumDue = 0;
    }
    
    mockTransactions.unshift({
      id: `TXN${Math.floor(Math.random() * 100000)}`,
      cardId: targetCardId,
      date: new Date().toISOString(),
      merchant: 'Credit Card Bill Payment',
      amount: paymentData.amount,
      type: 'Payment',
      status: 'Completed'
    });

    return { success: true, transactionId: `PAY${Math.floor(Math.random() * 100000)}` };
  },

  makePurchase: async (purchaseData, cardId = null) => {
    if (USE_REAL_API) {
      try {
        if (!cardId) {
          const userId = useAuthStore.getState().user?.id || 'admin';
          const response = await creditCardsApi.get(`/api/v1/credit-cards/user/${userId}`);
          const cards = response.data;
          if (cards.length === 0) throw new Error("No active card found");
          cardId = cards[0].id;
        }
        const response = await creditCardsApi.post(`/api/v1/credit-cards/${cardId}/purchase`, {
          merchant: purchaseData.merchant,
          amount: purchaseData.amount
        });
        return { success: true, transactionId: response.data.id || `TXN${Math.floor(Math.random() * 100000)}` };
      } catch (error) {
        console.error("Failed making credit card purchase:", error);
        throw new Error(error.response?.data?.message || "Failed to make purchase");
      }
    }

    await delay(1200);
    
    const targetCardId = cardId || mockCards[0]?.id;
    const cardIndex = mockCards.findIndex(c => c.id === targetCardId);
    
    if (cardIndex === -1) throw new Error("Card not found");
    const card = mockCards[cardIndex];

    if (!purchaseData.amount || purchaseData.amount <= 0) {
      throw new Error("Invalid purchase amount");
    }
    if (purchaseData.amount > card.availableCredit) {
      throw new Error("Insufficient credit limit");
    }

    card.availableCredit -= purchaseData.amount;
    card.outstandingAmount += purchaseData.amount;
    card.minimumDue = card.outstandingAmount * 0.05;
    
    mockTransactions.unshift({
      id: `TXN${Math.floor(Math.random() * 100000)}`,
      cardId: targetCardId,
      date: new Date().toISOString(),
      merchant: purchaseData.merchant || 'Mock Merchant',
      amount: purchaseData.amount,
      type: 'Purchase',
      status: 'Completed'
    });

    return { success: true, transactionId: `TXN${Math.floor(Math.random() * 100000)}` };
  }
};
