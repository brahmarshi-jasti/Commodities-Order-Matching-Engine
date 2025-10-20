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
  const [latencyData, setLatencyData] = useState<Array<{ time: number; latency: number }>>([])

  useEffect(() => {
    const socket = new SockJS('/ws')
    const stompClient = new Client({
      webSocketFactory: () => socket as any,
      onConnect: (frame: Frame) => {
        console.log('Connected: ' + frame)
        setIsConnected(true)

        stompClient.subscribe('/topic/trades', (message) => {
          const trade = JSON.parse(message.body)
          setTrades(prev => [trade, ...prev].slice(0, 50))
          
          setLatencyData(prev => {
            const newData = [...prev, {
              time: Date.now(),
              latency: trade.latencyMicros
            }].slice(-30)
            return newData
          })
        })
      },
      onDisconnect: () => {
        console.log('Disconnected')
        setIsConnected(false)
      },
      onStompError: (frame) => {
        console.error('STOMP error: ' + frame.headers['message'])
      }
    })

    stompClient.activate()

    const metricsInterval = setInterval(() => {
      fetch('/api/metrics')
        .then(res => res.json())
        .then(data => setMetrics(data))
        .catch(err => console.error('Error fetching metrics:', err))
    }, 1000)

    return () => {
      clearInterval(metricsInterval)
      stompClient.deactivate()
    }
  }, [])

  const commodities = ['OIL', 'GOLD', 'SILVER', 'COPPER', 'GAS']

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100">
      <div className="container mx-auto px-4 py-6">
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
      </div>
    </div>
  )
}

export default App
