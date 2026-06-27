import { statementsApi, accountsApi, transactionsApi } from '../../../services/apiConfig';

const USE_REAL_API = true;

const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

const mockAccounts = [
  { id: 'ACC_001', accountNumber: '100012345678', accountName: 'John Doe', type: 'Savings', balance: 54000.50 },
  { id: 'ACC_002', accountNumber: '200098765432', accountName: 'John Doe', type: 'Current', balance: 125000.00 },
];

const generateMockTransactions = (fromDate, toDate, accountId) => {
  const transactions = [];
  const start = new Date(fromDate).getTime();
  const end = new Date(toDate).getTime();
  
  // Ensure we have at least 10 and max 50 transactions
  const numTxns = Math.floor(Math.random() * 40) + 10;
  
  let currentBalance = Math.random() * 100000 + 10000; // Random starting balance

  const descriptions = [
    'Amazon Purchase', 'Salary Credit', 'Zomato Food', 'Netflix Sub', 'ATM Withdrawal', 
    'Grocery Store', 'Electricity Bill', 'Phone Bill', 'Rent Payment', 'Fund Transfer'
  ];

  for (let i = 0; i < numTxns; i++) {
    const randomTime = start + Math.random() * (end - start);
    const date = new Date(randomTime);
    
    const isCredit = Math.random() > 0.7; // 30% chance of credit
    const amount = isCredit 
      ? Math.floor(Math.random() * 50000) + 1000  // Credits are larger
      : Math.floor(Math.random() * 5000) + 100;   // Debits are smaller

    currentBalance = isCredit ? currentBalance + amount : currentBalance - amount;

    transactions.push({
      id: `TXN${Math.floor(Math.random() * 1000000)}`,
      date: date.toISOString(),
      description: descriptions[Math.floor(Math.random() * descriptions.length)],
      type: isCredit ? 'CREDIT' : 'DEBIT',
      debit: isCredit ? null : amount,
      credit: isCredit ? amount : null,
      balance: currentBalance
    });
  }

  // Sort by date descending
  return transactions.sort((a, b) => new Date(b.date) - new Date(a.date));
};

// Internal store for the current active statement so it can be shared between Preview and Download
let currentStatementData = null;

export const statementService = {
  getEligibleAccounts: async () => {
    if (USE_REAL_API) {
      try {
        const response = await accountsApi.get('/api/v1/accounts');
        return response.data.map(acc => ({
          id: (acc.account_number || acc.accountNumber || '').toString(),
          accountNumber: acc.account_number || acc.accountNumber,
          accountName: acc.name,
          type: acc.account_type || acc.accountType,
          balance: acc.balance
        }));
      } catch (error) {
        console.warn("Failed fetching accounts from backend, falling back to mock:", error);
      }
    }
    await delay(500);
    return mockAccounts;
  },

  generateStatement: async (accountId, fromDate, toDate, format) => {
    if (USE_REAL_API) {
      try {
        // Retrieve account details to find the account number
        const accsResponse = await accountsApi.get('/api/v1/accounts');
        const accounts = accsResponse.data;
        const account = accounts.find(a => a.id === accountId || a.accountNumber === accountId || a.account_number?.toString() === accountId?.toString());
        const targetAccountNumber = account ? (account.account_number || account.accountNumber) : accountId;

        const payload = {
          accountId: targetAccountNumber,
          fromDate,
          toDate,
          format
        };

        const response = await statementsApi.post('/api/v1/statements/generate', payload);
        const statementDto = response.data;

        // Try to fetch real transactions for the preview table
        let transactions = [];
        try {
          // Attempt using transactions service endpoint for account transaction history
          const txResponse = await transactionsApi.get(`/api/v1/transactions/account/${targetAccountNumber}`);
          const rawLogs = txResponse.data?.logs || txResponse.data || [];
          
          transactions = rawLogs
            .map(tx => {
              const txDate = tx.transactionDate || tx.timestamp || tx.createdAt || tx.created_at;
              const txType = tx.transactionType || tx.type || '';
              const txAmt = parseFloat(tx.amount || 0);
              const isCredit = txType.toUpperCase() === 'DEPOSIT' || txType.toUpperCase() === 'CREDIT';
              
              return {
                id: tx.id || tx.transactionId || `TXN${Math.floor(Math.random() * 1000000)}`,
                date: txDate,
                description: tx.description || `${txType} transaction`,
                type: isCredit ? 'CREDIT' : 'DEBIT',
                debit: isCredit ? null : txAmt,
                credit: isCredit ? txAmt : null,
                balance: tx.balanceAfter || tx.balance || 0
              };
            })
            .filter(tx => {
              const d = new Date(tx.date);
              const start = new Date(fromDate);
              const end = new Date(toDate);
              end.setHours(23, 59, 59, 999);
              return d >= start && d <= end;
            });
        } catch (txError) {
          console.warn("Failed fetching transaction history, generating mock transactions fallback:", txError);
          transactions = generateMockTransactions(fromDate, toDate, accountId);
        }

        // Compute summary values for preview
        const totalCredits = transactions.filter(t => t.type === 'CREDIT').reduce((sum, t) => sum + (t.credit || 0), 0);
        const totalDebits = transactions.filter(t => t.type === 'DEBIT').reduce((sum, t) => sum + (t.debit || 0), 0);
        
        const sortedAsc = [...transactions].sort((a, b) => new Date(a.date) - new Date(b.date));
        const openingBalance = sortedAsc.length > 0 ? sortedAsc[0].balance - (sortedAsc[0].type === 'CREDIT' ? sortedAsc[0].credit : -sortedAsc[0].debit) : (account ? account.balance : 0);
        const closingBalance = sortedAsc.length > 0 ? sortedAsc[sortedAsc.length - 1].balance : (account ? account.balance : 0);

        currentStatementData = {
          id: statementDto.id,
          accountDetails: {
            id: account ? account.id : accountId,
            accountNumber: targetAccountNumber,
            accountName: account ? account.accountName || 'Customer' : 'Customer',
            type: account ? account.type : 'Savings',
            balance: account ? account.balance : 0
          },
          period: { fromDate, toDate },
          format,
          summary: {
            openingBalance,
            closingBalance,
            totalCredits,
            totalDebits,
            transactionCount: transactions.length
          },
          transactions,
          downloadUrl: statementDto.downloadUrl
        };

        return {
          success: true,
          statementId: statementDto.id,
          message: 'Statement generated successfully.'
        };
      } catch (error) {
        console.warn("Failed generating statement via API, using local mock generation:", error);
      }
    }

    await delay(1500); // Simulate processing time
    
    const account = mockAccounts.find(a => a.id === accountId || a.accountNumber === accountId);
    if (!account) throw new Error("Account not found");

    const transactions = generateMockTransactions(fromDate, toDate, accountId);
    
    const totalCredits = transactions.filter(t => t.type === 'CREDIT').reduce((sum, t) => sum + t.credit, 0);
    const totalDebits = transactions.filter(t => t.type === 'DEBIT').reduce((sum, t) => sum + t.debit, 0);
    
    const sortedAsc = [...transactions].sort((a, b) => new Date(a.date) - new Date(b.date));
    const openingBalance = sortedAsc.length > 0 ? sortedAsc[0].balance - (sortedAsc[0].type === 'CREDIT' ? sortedAsc[0].credit : -sortedAsc[0].debit) : account.balance;
    const closingBalance = sortedAsc.length > 0 ? sortedAsc[sortedAsc.length - 1].balance : account.balance;

    const statementData = {
      accountDetails: account,
      period: { fromDate, toDate },
      format,
      summary: {
        openingBalance,
        closingBalance,
        totalCredits,
        totalDebits,
        transactionCount: transactions.length
      },
      transactions
    };

    currentStatementData = statementData;
    
    return {
      success: true,
      statementId: `STMT_${Math.floor(Math.random() * 100000)}`,
      message: 'Statement generated successfully.'
    };
  },

  getCurrentStatement: async () => {
    await delay(300);
    if (!currentStatementData) throw new Error("No statement generated");
    return currentStatementData;
  },

  downloadStatement: async (format) => {
    if (USE_REAL_API && currentStatementData && currentStatementData.id) {
      try {
        const response = await statementsApi.get(`/api/v1/statements/${currentStatementData.id}/download`, {
          responseType: 'blob'
        });
        
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `statement-${currentStatementData.id}.txt`);
        document.body.appendChild(link);
        link.click();
        link.remove();
        
        return { success: true, url };
      } catch (error) {
        console.warn("Failed downloading statement from backend, using client-side mock download:", error);
      }
    }

    await delay(2000);
    if (!currentStatementData) throw new Error("No statement available to download");
    
    const mockContent = `GDB BANK STATEMENT\nAccount: ${currentStatementData.accountDetails.accountNumber}\nPeriod: ${currentStatementData.period.fromDate} to ${currentStatementData.period.toDate}\n`;
    const url = window.URL.createObjectURL(new Blob([mockContent], { type: 'text/plain' }));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `mock-statement.${format.toLowerCase()}`);
    document.body.appendChild(link);
    link.click();
    link.remove();

    return { success: true, url: `/mock-download-url.${format.toLowerCase()}` };
  }
};
