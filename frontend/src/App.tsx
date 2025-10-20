import { useState, useEffect } from 'react'
import SockJS from 'sockjs-client'
import { Client, Frame } from 'stompjs'
import OrderBookPanel from './components/OrderBookPanel'
import TradePanel from './components/TradePanel'
import MetricsPanel from './components/MetricsPanel'
import LatencyChart from './components/LatencyChart'

interface Trade {
  tradeId: number
  commodity: string
  price: number
  quantity: number
  latencyMicros: number
  timestamp: number
}

interface Metrics {
  totalOrders: number
  totalTrades: number
  avgLatencyMicros: number
  commodities: Record<string, CommodityMetrics>
}

interface CommodityMetrics {
  commodity: string
  ordersReceived: number
  tradesExecuted: number
  fillRate: number
  avgSlippage: number
}

function App() {
  const [trades, setTrades] = useState<Trade[]>([])
  const [metrics, setMetrics] = useState<Metrics | null>(null)
  const [selectedCommodity, setSelectedCommodity] = useState<string>('OIL')
  const [isConnected, setIsConnected] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [latencyData, setLatencyData] = useState<Array<{ time: number; latency: number }>>([])
  const [reconnectAttempts, setReconnectAttempts] = useState(0)

  useEffect(() => {
    let socket: any
    let stompClient: Client
    
    const connect = () => {
      try {
        socket = new SockJS('/ws')
        stompClient = new Client({
          webSocketFactory: () => socket,
          onConnect: (frame: Frame) => {
            console.log('Connected: ' + frame)
            setIsConnected(true)
            setIsLoading(false)
            setError(null)
            setReconnectAttempts(0)

            stompClient.subscribe('/topic/trades', (message) => {
              try {
                const trade = JSON.parse(message.body)
                setTrades(prev => [trade, ...prev].slice(0, 50))
                
                setLatencyData(prev => {
                  const newData = [...prev, {
                    time: Date.now(),
                    latency: trade.latencyMicros
                  }].slice(-30)
                  return newData
                })
              } catch (err) {
                console.error('Error parsing trade message:', err)
              }
            })
          },
          onDisconnect: () => {
            console.log('Disconnected')
            setIsConnected(false)
            setError('Connection lost. Attempting to reconnect...')
            
            // Auto-reconnect after 3 seconds
            if (reconnectAttempts < 5) {
              setTimeout(() => {
                setReconnectAttempts(prev => prev + 1)
                connect()
              }, 3000)
            } else {
              setError('Failed to reconnect after multiple attempts')
            }
          },
          onStompError: (frame) => {
            console.error('STOMP error: ' + frame.headers['message'])
            setError('WebSocket error: ' + frame.headers['message'])
          }
        })

        stompClient.activate()
      } catch (err) {
        console.error('Connection error:', err)
        setError('Failed to establish connection')
        setIsLoading(false)
      }
    }
    
    connect()

    const metricsInterval = setInterval(() => {
      fetch('/api/metrics')
        .then(res => {
          if (!res.ok) {
            throw new Error(`HTTP ${res.status}: ${res.statusText}`)
          }
          return res.json()
        })
        .then(data => {
          setMetrics(data)
          setError(null)
        })
        .catch(err => {
          console.error('Error fetching metrics:', err)
          if (!isConnected) {
            setError('Unable to fetch metrics')
          }
        })
    }, 1000)

    return () => {
      clearInterval(metricsInterval)
      if (stompClient) {
        stompClient.deactivate()
      }
    }
  }, [reconnectAttempts])

  const commodities = ['OIL', 'GOLD', 'SILVER', 'COPPER', 'GAS']

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100">
      <div className="container mx-auto px-4 py-6">
        {/* Error Banner */}
        {error && (
          <div className="mb-4 bg-red-900/30 border border-red-700 text-red-300 px-4 py-3 rounded-lg flex items-center justify-between">
            <span>{error}</span>
            <button 
              onClick={() => setError(null)}
              className="text-red-400 hover:text-red-300"
            >
              ✕
            </button>
          </div>
        )}
        
        {/* Loading State */}
        {isLoading && (
          <div className="flex items-center justify-center py-20">
            <div className="text-center">
              <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-blue-500 mx-auto mb-4"></div>
              <p className="text-slate-400">Connecting to matching engine...</p>
            </div>
          </div>
        )}
        
        {!isLoading && (
          <>
            <div className="mb-6">
              <div className="flex items-center justify-between">
                <div>
                  <h1 className="text-4xl font-bold text-white mb-2">
                    Commodities Matching Engine
                  </h1>
                  <p className="text-slate-400">
                    Real-time order matching with sub-100µs latency
                  </p>
                </div>
                <div className="flex items-center gap-3">
                  <div className={`px-4 py-2 rounded-lg ${isConnected ? 'bg-green-900/30 text-green-400' : 'bg-red-900/30 text-red-400'}`}>
                    {isConnected ? '● Connected' : '○ Disconnected'}
                  </div>
                  {reconnectAttempts > 0 && (
                    <div className="text-xs text-slate-500">
                      Reconnect attempt {reconnectAttempts}/5
                    </div>
                  )}
                </div>
              </div>
            </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 mb-6">
          <div className="bg-slate-800 rounded-lg p-4 border border-slate-700">
            <div className="text-sm text-slate-400 mb-1">Total Orders</div>
            <div className="text-3xl font-bold text-blue-400">
              {metrics?.totalOrders.toLocaleString() || 0}
            </div>
          </div>
          <div className="bg-slate-800 rounded-lg p-4 border border-slate-700">
            <div className="text-sm text-slate-400 mb-1">Total Trades</div>
            <div className="text-3xl font-bold text-green-400">
              {metrics?.totalTrades.toLocaleString() || 0}
            </div>
          </div>
          <div className="bg-slate-800 rounded-lg p-4 border border-slate-700">
            <div className="text-sm text-slate-400 mb-1">Avg Latency</div>
            <div className="text-3xl font-bold text-purple-400">
              {metrics?.avgLatencyMicros.toFixed(2) || 0} µs
            </div>
          </div>
        </div>

        <div className="mb-4">
          <div className="flex gap-2">
            {commodities.map(commodity => (
              <button
                key={commodity}
                onClick={() => setSelectedCommodity(commodity)}
                className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                  selectedCommodity === commodity
                    ? 'bg-blue-600 text-white'
                    : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
                }`}
              >
                {commodity}
              </button>
            ))}
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
          <OrderBookPanel commodity={selectedCommodity} />
          <LatencyChart data={latencyData} />
        </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <TradePanel trades={trades.filter(t => t.commodity === selectedCommodity)} />
              <MetricsPanel metrics={metrics} selectedCommodity={selectedCommodity} />
            </div>
          </>
        )}
      </div>
    </div>
  )
}

export default App
