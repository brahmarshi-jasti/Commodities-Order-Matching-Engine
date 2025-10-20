interface CommodityMetrics {
  commodity: string
  ordersReceived: number
  tradesExecuted: number
  fillRate: number
  avgSlippage: number
}

interface Metrics {
  totalOrders: number
  totalTrades: number
  avgLatencyMicros: number
  commodities: Record<string, CommodityMetrics>
}

interface Props {
  metrics: Metrics | null
  selectedCommodity: string
}

export default function MetricsPanel({ metrics, selectedCommodity }: Props) {
  if (!metrics) {
    return <div className="bg-slate-800 rounded-lg p-6 border border-slate-700">Loading metrics...</div>
  }

  const commodityMetrics = metrics.commodities[selectedCommodity]

  if (!commodityMetrics) {
    return <div className="bg-slate-800 rounded-lg p-6 border border-slate-700">No data available</div>
  }

  return (
    <div className="bg-slate-800 rounded-lg p-6 border border-slate-700">
      <h2 className="text-xl font-bold mb-4 text-white">Performance Metrics - {selectedCommodity}</h2>
      
      <div className="grid grid-cols-2 gap-4">
        <div className="bg-slate-700/50 rounded-lg p-4">
          <div className="text-xs text-slate-400 mb-1">Orders Received</div>
          <div className="text-2xl font-bold text-blue-400">
            {commodityMetrics.ordersReceived.toLocaleString()}
          </div>
        </div>

        <div className="bg-slate-700/50 rounded-lg p-4">
          <div className="text-xs text-slate-400 mb-1">Trades Executed</div>
          <div className="text-2xl font-bold text-green-400">
            {commodityMetrics.tradesExecuted.toLocaleString()}
          </div>
        </div>

        <div className="bg-slate-700/50 rounded-lg p-4">
          <div className="text-xs text-slate-400 mb-1">Fill Rate</div>
          <div className="text-2xl font-bold text-yellow-400">
            {commodityMetrics.fillRate.toFixed(1)}%
          </div>
        </div>

        <div className="bg-slate-700/50 rounded-lg p-4">
          <div className="text-xs text-slate-400 mb-1">Avg Slippage</div>
          <div className="text-2xl font-bold text-purple-400">
            ${commodityMetrics.avgSlippage.toFixed(3)}
          </div>
        </div>
      </div>

      <div className="mt-6">
        <h3 className="text-sm font-semibold text-slate-400 mb-3">All Commodities</h3>
        <div className="space-y-2">
          {Object.values(metrics.commodities).map(cm => (
            <div key={cm.commodity} className="flex items-center justify-between text-sm py-2 px-3 bg-slate-700/30 rounded">
              <span className="font-medium text-slate-300">{cm.commodity}</span>
              <div className="flex gap-4 text-xs">
                <span className="text-blue-400">{cm.ordersReceived} orders</span>
                <span className="text-green-400">{cm.tradesExecuted} trades</span>
                <span className="text-yellow-400">{cm.fillRate.toFixed(0)}% fill</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
