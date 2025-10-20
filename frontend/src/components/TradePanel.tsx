interface Trade {
  tradeId: number
  commodity: string
  price: number
  quantity: number
  latencyMicros: number
  timestamp: number
}

interface Props {
  trades: Trade[]
}

export default function TradePanel({ trades }: Props) {
  return (
    <div className="bg-slate-800 rounded-lg p-6 border border-slate-700">
      <h2 className="text-xl font-bold mb-4 text-white">Recent Trades</h2>
      
      <div className="overflow-y-auto max-h-96">
        <table className="w-full text-sm">
          <thead className="sticky top-0 bg-slate-800">
            <tr className="border-b border-slate-700">
              <th className="text-left py-2 text-slate-400">Trade ID</th>
              <th className="text-right py-2 text-slate-400">Price</th>
              <th className="text-right py-2 text-slate-400">Quantity</th>
              <th className="text-right py-2 text-slate-400">Latency (µs)</th>
            </tr>
          </thead>
          <tbody>
            {trades.map(trade => (
              <tr key={trade.tradeId} className="border-b border-slate-700/50 hover:bg-slate-700/30">
                <td className="py-2 text-slate-300">#{trade.tradeId}</td>
                <td className="text-right font-mono text-blue-400">${trade.price.toFixed(2)}</td>
                <td className="text-right text-slate-300">{trade.quantity}</td>
                <td className="text-right font-mono text-purple-400">{trade.latencyMicros.toFixed(2)}</td>
              </tr>
            ))}
          </tbody>
        </table>
        
        {trades.length === 0 && (
          <div className="text-center text-slate-500 py-8">
            No trades yet for this commodity
          </div>
        )}
      </div>
    </div>
  )
}
