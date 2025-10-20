import { useState, useEffect } from 'react'

interface Order {
  orderId: number
  price: number
  quantity: number
  side: string
}

interface OrderBookData {
  commodity: string
  bids: Order[]
  asks: Order[]
  bestBid: number
  bestAsk: number
  spread: number
}

interface Props {
  commodity: string
}

export default function OrderBookPanel({ commodity }: Props) {
  const [orderBook, setOrderBook] = useState<OrderBookData | null>(null)

  useEffect(() => {
    const fetchOrderBook = () => {
      fetch(`/api/orderbook/${commodity}`)
        .then(res => res.json())
        .then(data => setOrderBook(data))
        .catch(err => console.error('Error fetching order book:', err))
    }

    fetchOrderBook()
    const interval = setInterval(fetchOrderBook, 500)

    return () => clearInterval(interval)
  }, [commodity])

  if (!orderBook) {
    return <div className="bg-slate-800 rounded-lg p-6 border border-slate-700">Loading...</div>
  }

  return (
    <div className="bg-slate-800 rounded-lg p-6 border border-slate-700">
      <h2 className="text-xl font-bold mb-4 text-white">Order Book - {commodity}</h2>
      
      <div className="grid grid-cols-3 gap-4 mb-4">
        <div className="text-center">
          <div className="text-xs text-slate-400 mb-1">Best Bid</div>
          <div className="text-lg font-bold text-green-400">${orderBook.bestBid.toFixed(2)}</div>
        </div>
        <div className="text-center">
          <div className="text-xs text-slate-400 mb-1">Spread</div>
          <div className="text-lg font-bold text-yellow-400">${orderBook.spread.toFixed(2)}</div>
        </div>
        <div className="text-center">
          <div className="text-xs text-slate-400 mb-1">Best Ask</div>
          <div className="text-lg font-bold text-red-400">${orderBook.bestAsk.toFixed(2)}</div>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <div className="text-sm font-semibold mb-2 text-green-400">Bids (Buy)</div>
          <div className="space-y-1">
            {orderBook.bids.slice(0, 10).map((bid, idx) => (
              <div key={idx} className="flex justify-between text-sm bg-green-900/20 px-2 py-1 rounded">
                <span className="text-green-400 font-mono">${bid.price.toFixed(2)}</span>
                <span className="text-slate-300">{bid.quantity}</span>
              </div>
            ))}
          </div>
        </div>

        <div>
          <div className="text-sm font-semibold mb-2 text-red-400">Asks (Sell)</div>
          <div className="space-y-1">
            {orderBook.asks.slice(0, 10).map((ask, idx) => (
              <div key={idx} className="flex justify-between text-sm bg-red-900/20 px-2 py-1 rounded">
                <span className="text-red-400 font-mono">${ask.price.toFixed(2)}</span>
                <span className="text-slate-300">{ask.quantity}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
