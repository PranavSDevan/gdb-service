import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { creditCardService } from '../services/mockCreditCardService';
import CreditCardWidget from '../components/CreditCardWidget';
import UtilizationBar from '../components/UtilizationBar';
import { AlertCircle, CreditCard, ArrowRight, CheckCircle, ChevronDown, Receipt, FileText, ShoppingBag } from 'lucide-react';
import toast from 'react-hot-toast';
import useSettingsStore from '../../../store/settingsStore';

const CreditCardDashboard = () => {
  const [cards, setCards] = useState([]);
  const [selectedCardId, setSelectedCardId] = useState('');
  
  const [data, setData] = useState(null);
  const [recentTransactions, setRecentTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterType, setFilterType] = useState('All');
  const [refreshKey, setRefreshKey] = useState(0);

  // Purchase Simulation State
  const [showPurchaseModal, setShowPurchaseModal] = useState(false);
  const [merchant, setMerchant] = useState('Amazon');
  const [purchaseAmount, setPurchaseAmount] = useState('500');
  const [simulating, setSimulating] = useState(false);
  
  const navigate = useNavigate();
  const formatCurrency = useSettingsStore((state) => state.formatCurrencyAmount);
  const t = useSettingsStore((state) => state.t);

  // Load all cards initially
  useEffect(() => {
    const fetchInitialData = async () => {
      try {
        setLoading(true);
        const allCards = await creditCardService.getAllCards();
        setCards(allCards);
        
        if (allCards && allCards.length > 0) {
          setSelectedCardId(allCards[0].id);
        } else {
          setLoading(false); // No cards, stop loading
        }
      } catch (error) {
        toast.error('Unable to Load Cards');
        setLoading(false);
      }
    };
    fetchInitialData();
  }, [refreshKey]);

  // Reload specific card data when selection changes or refreshKey updates
  useEffect(() => {
    if (!selectedCardId) return;

    const loadCardData = async () => {
      try {
        setLoading(true);
        const cardData = await creditCardService.getDashboardData(selectedCardId);
        setData(cardData);
        
        if (cardData) {
          const txns = await creditCardService.getTransactions(null, selectedCardId);
          setRecentTransactions(txns.slice(0, 5));
        }
      } catch (error) {
        toast.error('Unable to Load Card Details');
      } finally {
        setLoading(false);
      }
    };
    loadCardData();
  }, [selectedCardId, refreshKey]);

  const isPaymentDueSoon = () => {
    if (!data) return false;
    const dueDate = new Date(data.nextDueDate);
    const today = new Date();
    const diffTime = dueDate - today;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays <= 7 && diffDays >= 0;
  };

  const showWarning = isPaymentDueSoon() && data.outstandingAmount > 0;

  const filteredTransactions = recentTransactions.filter(
    tx => filterType === 'All' ? true : tx.type === filterType
  );

  const handleSimulatePurchase = async (e) => {
    e.preventDefault();
    if (!purchaseAmount || isNaN(parseFloat(purchaseAmount)) || parseFloat(purchaseAmount) <= 0) {
      toast.error('Please enter a valid amount');
      return;
    }
    try {
      setSimulating(true);
      await creditCardService.makePurchase({
        merchant,
        amount: parseFloat(purchaseAmount)
      }, selectedCardId);
      
      toast.success('Purchase Simulated Successfully');
      setShowPurchaseModal(false);
      setRefreshKey(prev => prev + 1);
    } catch (error) {
      toast.error(error.message || 'Simulation Failed');
    } finally {
      setSimulating(false);
    }
  };

  if (loading && cards.length === 0) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto space-y-6 pb-12">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">{t('cardDashboard')}</h1>
          <p className="text-gray-500">Manage your credit card and view statements</p>
        </div>
        
        <div className="flex flex-wrap items-center gap-3">
          {/* Card Selector Dropdown */}
          {cards.length > 0 && (
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <CreditCard className="h-4 w-4 text-gray-400" />
              </div>
              <select 
                value={selectedCardId}
                onChange={(e) => setSelectedCardId(e.target.value)}
                className="appearance-none bg-white border border-gray-300 text-gray-700 py-2.5 pl-10 pr-10 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 font-medium cursor-pointer"
              >
                {cards.map(c => {
                  const num = c.cardNumber || '';
                  const last4 = num.replace(/\s+/g, '').slice(-4);
                  return (
                    <option key={c.id} value={c.id}>
                      {c.cardType} ({last4})
                    </option>
                  );
                })}
              </select>
              <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-3 text-gray-500">
                <ChevronDown className="w-4 h-4" />
              </div>
            </div>
          )}

          {cards.length > 0 && (
            <button
              onClick={() => setShowPurchaseModal(true)}
              className="btn-secondary flex items-center gap-2 shadow-sm border border-gray-300 bg-white hover:bg-gray-50 py-2.5 px-4 rounded-lg font-medium"
            >
              <ShoppingBag className="w-4 h-4 text-gray-600" />
              {t('simulatePurchase')}
            </button>
          )}

          <button
            onClick={() => navigate('/credit-cards/apply')}
            className="btn-primary flex items-center gap-2 shadow-sm py-2.5 px-4 rounded-lg bg-primary-600 hover:bg-primary-700 text-white font-medium"
          >
            <CreditCard className="w-4 h-4" />
            {t('applyCreditCard')}
          </button>
        </div>
      </div>

      {loading && cards.length > 0 && (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
        </div>
      )}

      {!loading && showWarning && (
        <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4 rounded-md shadow-sm flex items-start">
          <AlertCircle className="w-5 h-5 text-yellow-500 mt-0.5 mr-3 flex-shrink-0" />
          <div>
            <h3 className="text-sm font-medium text-yellow-800">{t('paymentDueSoon')}</h3>
            <p className="text-sm text-yellow-700 mt-1">
              Your minimum payment of {formatCurrency(data.minimumDue)} is due on {new Date(data.nextDueDate).toLocaleDateString()}. Please pay on time to avoid late fees.
            </p>
          </div>
        </div>
      )}

      {!loading && data?.outstandingAmount === 0 && (
        <div className="bg-green-50 border-l-4 border-green-400 p-4 rounded-md shadow-sm flex items-start mb-4">
          <CheckCircle className="w-5 h-5 text-green-500 mt-0.5 mr-3 flex-shrink-0" />
          <div>
            <h3 className="text-sm font-medium text-green-800">{t('outstandingAmount')}: {formatCurrency(0)}</h3>
            <p className="text-sm text-green-700 mt-1">
              You have no pending dues. Please add more money or spend more!
            </p>
          </div>
        </div>
      )}

      {!loading && data ? (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Card Visual & Util */}
          <div className="lg:col-span-1 space-y-6">
            <CreditCardWidget data={data} />
            <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
              <UtilizationBar available={data.availableCredit} limit={data.creditLimit} />
            </div>
          </div>

          {/* Details & Transactions */}
          <div className="lg:col-span-2 space-y-6">
            
            {/* Account Summary */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
              <div className="px-6 py-4 border-b border-gray-100 bg-gray-50/50 flex justify-between items-center">
                <h3 className="font-semibold text-gray-800">{t('accountSummary')}</h3>
                <span className="text-xs font-medium bg-primary-100 text-primary-700 px-2.5 py-1 rounded-full">
                  {data.status}
                </span>
              </div>
              <div className="p-6 grid grid-cols-1 sm:grid-cols-2 gap-6">
                <div>
                  <p className="text-sm text-gray-500">{t('outstandingAmount')}</p>
                  <p className="text-2xl font-bold text-gray-900">{formatCurrency(data.outstandingAmount)}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">{t('availableCredit')}</p>
                  <p className="text-2xl font-bold text-primary-600">{formatCurrency(data.availableCredit)}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">{t('minimumDue')}</p>
                  <p className="text-lg font-semibold text-gray-800">{formatCurrency(data.minimumDue)}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">{t('nextDueDate')}</p>
                  <p className="text-lg font-semibold text-gray-800">{new Date(data.nextDueDate).toLocaleDateString()}</p>
                </div>
              </div>
            </div>

            {/* Dashboard Recent Transactions with Dropdown */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
              <div className="px-6 py-4 border-b border-gray-100 bg-white flex justify-between items-center">
                <h3 className="font-semibold text-gray-800">{t('recentActivity')}</h3>
                <div className="relative">
                  <select 
                    value={filterType}
                    onChange={(e) => setFilterType(e.target.value)}
                    className="appearance-none bg-gray-50 border border-gray-200 text-gray-700 py-1.5 pl-3 pr-8 rounded-md text-sm focus:outline-none focus:ring-1 focus:ring-primary-500 focus:border-primary-500 cursor-pointer"
                  >
                    <option value="All">All Transactions</option>
                    <option value="Purchase">Purchases Only</option>
                    <option value="Payment">Payments Only</option>
                  </select>
                  <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-gray-500">
                    <ChevronDown className="w-3.5 h-3.5" />
                  </div>
                </div>
              </div>
              
              <div className="divide-y divide-gray-100">
                {filteredTransactions.length === 0 ? (
                  <div className="p-6 text-center text-gray-500 text-sm">
                    No recent activity found for this filter.
                  </div>
                ) : (
                  filteredTransactions.map(tx => (
                    <div key={tx.id} className="p-4 px-6 flex justify-between items-center hover:bg-gray-50 transition-colors">
                      <div className="flex items-center gap-3">
                        <div className={`w-10 h-10 rounded-full flex items-center justify-center ${
                          tx.type === 'Payment' ? 'bg-green-100 text-green-600' : 'bg-blue-100 text-blue-600'
                        }`}>
                          {tx.type === 'Payment' ? <Receipt className="w-5 h-5" /> : <FileText className="w-5 h-5" />}
                        </div>
                        <div>
                          <p className="text-sm font-medium text-gray-900">{tx.merchant}</p>
                          <p className="text-xs text-gray-500">{new Date(tx.date).toLocaleDateString()}</p>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className={`text-sm font-bold ${tx.type === 'Payment' ? 'text-green-600' : 'text-gray-900'}`}>
                          {tx.type === 'Payment' ? '+' : '-'} {formatCurrency(tx.amount)}
                        </p>
                        <p className="text-xs text-gray-500">{tx.status}</p>
                      </div>
                    </div>
                  ))
                )}
              </div>
              
              <div className="px-6 py-3 border-t border-gray-100 bg-gray-50 text-center">
                <button 
                  onClick={() => navigate('/credit-cards/transactions', { state: { cardId: selectedCardId } })}
                  className="text-sm text-primary-600 hover:text-primary-700 font-medium flex items-center justify-center w-full gap-1"
                >
                  View All Transactions <ArrowRight className="w-4 h-4" />
                </button>
              </div>
            </div>

          </div>
        </div>
      ) : !loading && cards.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-xl shadow-sm border border-gray-100">
          <CreditCard className="w-16 h-16 text-gray-300 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900">No active credit card</h3>
          <p className="text-gray-500 mt-2 mb-6">Apply for a new credit card to get started.</p>
          <button
            onClick={() => navigate('/credit-cards/apply')}
            className="btn-primary"
          >
            Apply Now
          </button>
        </div>
      ) : null}

      {/* Simulate Purchase Modal */}
      {showPurchaseModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-lg border border-gray-100 max-w-md w-full overflow-hidden">
            <div className="px-6 py-4 bg-gray-50 border-b border-gray-150 flex justify-between items-center">
              <h3 className="font-bold text-gray-900 text-lg flex items-center gap-2">
                <ShoppingBag className="w-5 h-5 text-primary-600" />
                {t('simulatePurchase')}
              </h3>
              <button 
                onClick={() => setShowPurchaseModal(false)}
                className="text-gray-400 hover:text-gray-600 text-xl font-bold"
              >
                &times;
              </button>
            </div>
            <form onSubmit={handleSimulatePurchase} className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Merchant</label>
                <input 
                  type="text"
                  required
                  value={merchant}
                  onChange={(e) => setMerchant(e.target.value)}
                  className="w-full rounded-lg border-gray-300 shadow-sm focus:ring-primary-500 focus:border-primary-500 border p-2.5"
                  placeholder="e.g. Amazon, Uber, Starbucks"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Amount (₹)</label>
                <input 
                  type="number"
                  required
                  min="1"
                  value={purchaseAmount}
                  onChange={(e) => setPurchaseAmount(e.target.value)}
                  className="w-full rounded-lg border-gray-300 shadow-sm focus:ring-primary-500 focus:border-primary-500 border p-2.5"
                  placeholder="e.g. 500"
                />
              </div>
              <div className="pt-2 flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setShowPurchaseModal(false)}
                  className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={simulating}
                  className="px-4 py-2 text-sm font-medium text-white bg-primary-600 rounded-lg hover:bg-primary-700 flex items-center gap-2"
                >
                  {simulating && <div className="animate-spin rounded-full h-4 w-4 border-2 border-white border-t-transparent" />}
                  Submit Purchase
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default CreditCardDashboard;
