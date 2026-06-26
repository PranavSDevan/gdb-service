import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Sparkles, Send, X } from 'lucide-react';
import { aiAssistantService } from '../services/aiAssistantService';
import toast from 'react-hot-toast';

const SUGGESTIONS = [
  { text: '💳 Apply Credit Card', query: 'apply for a credit card' },
  { text: '📄 View Statements', query: 'show me my bank statement' },
  { text: '⚙️ Open Settings', query: 'open settings' },
  { text: '📊 Dashboard Overview', query: 'go to dashboard' },
  { text: '💸 Send Money', query: 'transfer money' }
];

const AiAssistantWidget = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([
    {
      id: 'welcome',
      text: "Hello! I'm GDB Copilot, your virtual banking assistant.\n\nAsk me questions or click the chips below to navigate anywhere instantly!",
      isBot: true,
      timestamp: new Date()
    }
  ]);
  const [inputVal, setInputVal] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef(null);
  const navigate = useNavigate();

  // Scroll to bottom on new messages
  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages, isLoading]);

  const handleSend = async (queryText) => {
    const textToSend = queryText || inputVal;
    if (!textToSend.trim()) return;

    // Clear input
    if (!queryText) setInputVal('');

    // Add user message
    const userMsg = {
      id: `user-${Date.now()}`,
      text: textToSend,
      isBot: false,
      timestamp: new Date()
    };
    setMessages(prev => [...prev, userMsg]);

    setIsLoading(true);

    try {
      const result = await aiAssistantService.processQuery(textToSend);
      
      const botMsg = {
        id: `bot-${Date.now()}`,
        text: result.text,
        isBot: true,
        timestamp: new Date()
      };

      setMessages(prev => [...prev, botMsg]);

      if (result.route) {
        // Trigger automated redirection
        const pageName = result.route.replace('/', '').replace('-', ' ');
        toast.success(`Redirecting to ${pageName.toUpperCase()}...`, {
          icon: '🚀',
          duration: 2000
        });
        
        setTimeout(() => {
          setIsOpen(false);
          navigate(result.route);
        }, 1200);
      }
    } catch (err) {
      toast.error('Copilot encountered an issue.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="print:hidden">
      {/* Floating Toggle Button */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="fixed bottom-6 right-6 z-50 flex items-center justify-center w-14 h-14 bg-gradient-to-tr from-primary-600 to-indigo-600 text-white rounded-full shadow-2xl hover:shadow-primary-500/30 transition-all duration-300 hover:scale-105 active:scale-95 group focus:outline-none"
        aria-label="Toggle GDB Copilot AI Assistant"
      >
        <span className="absolute -inset-1 rounded-full bg-gradient-to-tr from-primary-500 to-indigo-500 opacity-30 blur group-hover:opacity-50 transition duration-300"></span>
        {isOpen ? (
          <X className="w-6 h-6 relative z-10" />
        ) : (
          <Sparkles className="w-6 h-6 relative z-10 animate-pulse" />
        )}
      </button>

      {/* Chat Window */}
      {isOpen && (
        <div className="fixed bottom-24 right-6 w-96 max-w-[calc(100vw-2rem)] h-[500px] z-50 flex flex-col bg-white border border-gray-100 rounded-2xl shadow-2xl overflow-hidden transition-all duration-300 transform scale-100 origin-bottom-right">
          
          {/* Header */}
          <div className="px-5 py-4 bg-gradient-to-r from-primary-600 to-indigo-600 text-white flex items-center justify-between shadow-sm">
            <div className="flex items-center gap-2.5">
              <div className="relative">
                <div className="w-9 h-9 bg-white/10 rounded-xl flex items-center justify-center">
                  <Sparkles className="w-5 h-5 text-amber-300" />
                </div>
                <span className="absolute bottom-0 right-0 w-2.5 h-2.5 bg-green-400 border-2 border-white rounded-full animate-ping"></span>
                <span className="absolute bottom-0 right-0 w-2.5 h-2.5 bg-green-400 border-2 border-white rounded-full"></span>
              </div>
              <div>
                <h3 className="font-semibold text-sm leading-tight">GDB Copilot</h3>
                <p className="text-[10px] text-white/80">AI Assistant • Online</p>
              </div>
            </div>
            <button 
              onClick={() => setIsOpen(false)}
              className="text-white/80 hover:text-white transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Messages Container */}
          <div className="flex-1 overflow-y-auto p-4 space-y-3.5 bg-gray-50/50">
            {messages.map(msg => (
              <div
                key={msg.id}
                className={`flex flex-col ${msg.isBot ? 'items-start' : 'items-end'}`}
              >
                <div
                  className={`px-4 py-2.5 text-sm shadow-sm whitespace-pre-line max-w-[85%] ${
                    msg.isBot
                      ? 'bg-white text-gray-800 border border-gray-100 rounded-2xl rounded-tl-none'
                      : 'bg-primary-600 text-white rounded-2xl rounded-tr-none'
                  }`}
                >
                  {msg.text}
                </div>
                <span className="text-[9px] text-gray-400 mt-1 px-1">
                  {msg.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </span>
              </div>
            ))}

            {/* Typing Indicator */}
            {isLoading && (
              <div className="flex flex-col items-start">
                <div className="bg-white border border-gray-100 rounded-2xl rounded-tl-none px-4 py-3 shadow-sm flex items-center gap-1">
                  <div className="w-1.5 h-1.5 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0ms' }}></div>
                  <div className="w-1.5 h-1.5 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '150ms' }}></div>
                  <div className="w-1.5 h-1.5 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '300ms' }}></div>
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {/* Suggestion Chips */}
          <div className="px-4 py-2 bg-white border-t border-gray-100 flex items-center gap-2 overflow-x-auto no-scrollbar scroll-smooth">
            {SUGGESTIONS.map((chip, idx) => (
              <button
                key={idx}
                onClick={() => handleSend(chip.query)}
                className="px-3 py-1.5 bg-gray-50 border border-gray-200 hover:bg-primary-50 hover:border-primary-200 text-gray-700 hover:text-primary-600 rounded-full text-xs font-medium transition-all duration-200 whitespace-nowrap focus:outline-none"
              >
                {chip.text}
              </button>
            ))}
          </div>

          {/* Footer Input Bar */}
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleSend();
            }}
            className="p-3 bg-white border-t border-gray-100 flex items-center gap-2"
          >
            <input
              type="text"
              maxLength={150}
              placeholder="Ask me something or navigate..."
              value={inputVal}
              onChange={(e) => setInputVal(e.target.value)}
              className="flex-1 px-4 py-2 bg-gray-50 border border-gray-200 focus:border-primary-500 rounded-xl text-sm focus:outline-none transition-colors"
              disabled={isLoading}
            />
            <button
              type="submit"
              disabled={!inputVal.trim() || isLoading}
              className="p-2 bg-primary-600 hover:bg-primary-700 text-white rounded-xl transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Send className="w-4 h-4" />
            </button>
          </form>

        </div>
      )}
    </div>
  );
};

export default AiAssistantWidget;
