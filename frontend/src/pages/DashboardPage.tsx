import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { CreditCard, Coins, History, Loader2, Medal, RotateCw, ShoppingCart, Sparkles, Trophy, Wallet, X } from 'lucide-react';
import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { useSearchParams } from 'react-router-dom';
import { api } from '../api/endpoints';
import { getApiError } from '../api/axiosClient';
import { EmptyState } from '../components/EmptyState';
import { ErrorState } from '../components/ErrorState';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { Pagination } from '../components/Pagination';
import { StatCard } from '../components/StatCard';
import { useAuthStore } from '../store/authStore';
import type { BuyTurnsRequest, GuessResponse } from '../types';
import { formatCurrency, formatDateTime } from '../utils/format';

const queryKeys = {
  me: ['me'],
  leaderboard: ['leaderboard'],
  guessHistory: (page: number) => ['guess-history', page],
  purchaseHistory: (page: number) => ['purchase-history', page]
};

export function DashboardPage() {
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();
  const updateUser = useAuthStore((state) => state.updateUser);
  const [guessPage, setGuessPage] = useState(0);
  const [purchasePage, setPurchasePage] = useState(0);
  const [lastGuess, setLastGuess] = useState<GuessResponse | null>(null);
  const [isBuyModalOpen, setIsBuyModalOpen] = useState(false);

  const meQuery = useQuery({ queryKey: queryKeys.me, queryFn: api.me });
  const leaderboardQuery = useQuery({ queryKey: queryKeys.leaderboard, queryFn: api.leaderboard });
  const guessHistoryQuery = useQuery({ queryKey: queryKeys.guessHistory(guessPage), queryFn: () => api.guessHistory(guessPage, 8) });
  const purchaseHistoryQuery = useQuery({
    queryKey: queryKeys.purchaseHistory(purchasePage),
    queryFn: () => api.purchaseHistory(purchasePage, 8)
  });

  useEffect(() => {
    if (meQuery.data) {
      updateUser(meQuery.data);
    }
  }, [meQuery.data, updateUser]);

  useEffect(() => {
    const paymentStatus = searchParams.get('paymentStatus');
    if (!paymentStatus) {
      return;
    }

    const message = searchParams.get('message');
    if (paymentStatus === 'success') {
      toast.success(message || 'Thanh toan thanh cong.');
    } else {
      toast.error(message || 'Thanh toan khong thanh cong.');
    }

    void queryClient.invalidateQueries({ queryKey: queryKeys.me });
    void queryClient.invalidateQueries({ queryKey: ['purchase-history'] });
    setSearchParams({}, { replace: true });
  }, [queryClient, searchParams, setSearchParams]);

  const guessMutation = useMutation({
    mutationFn: api.guess,
    onSuccess: (data) => {
      setLastGuess(data);
      toast.success(data.message);
      void queryClient.invalidateQueries({ queryKey: queryKeys.me });
      void queryClient.invalidateQueries({ queryKey: ['guess-history'] });
      if (data.correct) {
        void queryClient.invalidateQueries({ queryKey: queryKeys.leaderboard });
      }
    },
    onError: (error) => toast.error(getApiError(error).message)
  });

  const buyTurnsMutation = useMutation({
    mutationFn: api.buyTurns,
    onSuccess: (data) => {
      if (data.paymentUrl) {
        toast.success(data.message);
        window.location.assign(data.paymentUrl);
        return;
      }
      toast.success(data.message);
      setIsBuyModalOpen(false);
      void queryClient.invalidateQueries({ queryKey: queryKeys.me });
      void queryClient.invalidateQueries({ queryKey: ['purchase-history'] });
    },
    onError: (error) => toast.error(getApiError(error).message)
  });

  const handleBuyTurns = (provider: BuyTurnsRequest['provider']) => {
    buyTurnsMutation.mutate({ provider });
  };

  if (meQuery.isLoading) {
    return <LoadingSpinner label="Dang tai dashboard..." />;
  }

  if (meQuery.isError || !meQuery.data) {
    return <ErrorState message="Khong tai duoc thong tin nguoi choi." onRetry={() => void meQuery.refetch()} />;
  }

  const user = meQuery.data;

  return (
    <div className="space-y-6">
      <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Diem" value={user.score} icon={Sparkles} tone="emerald" />
        <StatCard label="Luot con lai" value={user.turns} icon={Coins} tone="amber" />
        <StatCard label="Hang cua ban" value={`#${user.rank ?? '-'}`} icon={Medal} tone="sky" />
        <StatCard label="Vai tro" value={user.role} icon={Trophy} tone="rose" />
      </section>

      <section className="grid gap-6 lg:grid-cols-[minmax(0,1.2fr)_minmax(320px,0.8fr)]">
        <div className="rounded-lg border border-slate-200 bg-white p-5">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h1 className="text-xl font-bold text-slate-950">Chon mot so</h1>
              <p className="text-sm text-slate-500">Ty le thang cau hinh 5%, moi lan doan tru 1 luot.</p>
            </div>
            <button
              type="button"
              className="secondary-button"
              onClick={() => setIsBuyModalOpen(true)}
              disabled={buyTurnsMutation.isPending}
            >
              {buyTurnsMutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" /> : <ShoppingCart className="h-4 w-4" aria-hidden="true" />}
              Mua them 5 luot
            </button>
          </div>

          <div className="mt-6 grid grid-cols-5 gap-2 sm:gap-3">
            {[1, 2, 3, 4, 5].map((number) => (
              <button
                key={number}
                type="button"
                disabled={guessMutation.isPending || user.turns <= 0}
                onClick={() => guessMutation.mutate({ number })}
                className="aspect-square rounded-lg border border-slate-200 bg-slate-50 text-2xl font-bold text-slate-900 transition hover:border-emerald-400 hover:bg-emerald-50 focus:outline-none focus:ring-2 focus:ring-emerald-500 disabled:cursor-not-allowed disabled:opacity-50"
                aria-label={`Doan so ${number}`}
              >
                {number}
              </button>
            ))}
          </div>

          {lastGuess ? (
            <div className={`mt-5 rounded-lg border p-4 ${lastGuess.correct ? 'border-emerald-200 bg-emerald-50' : 'border-amber-200 bg-amber-50'}`}>
              <p className="font-semibold text-slate-950">
                {lastGuess.result}: ban chon {lastGuess.guessedNumber}, server la {lastGuess.serverNumber}
              </p>
              <p className="mt-1 text-sm text-slate-700">
                Diem {lastGuess.score}, con {lastGuess.remainingTurns} luot, luc {formatDateTime(lastGuess.playedAt)}.
              </p>
            </div>
          ) : (
            <div className="mt-5 rounded-lg border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
              Ket qua luot moi nhat se hien thi tai day.
            </div>
          )}

          {user.turns <= 0 ? (
            <div className="mt-4 rounded-lg border border-rose-200 bg-rose-50 p-4 text-sm text-rose-800">
              Ban da het luot choi. Hay mua them luot de tiep tuc.
            </div>
          ) : null}
        </div>

        <LeaderboardPanel isLoading={leaderboardQuery.isLoading} isError={leaderboardQuery.isError} onRetry={() => void leaderboardQuery.refetch()} items={leaderboardQuery.data?.items ?? []} />
      </section>

      {isBuyModalOpen ? (
        <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/50 px-4 py-6">
          <div className="w-full max-w-md rounded-lg bg-white shadow-xl">
            <div className="flex items-center justify-between border-b border-slate-200 px-5 py-4">
              <div>
                <h2 className="text-lg font-bold text-slate-950">Chon cach mua luot</h2>
                <p className="text-sm text-slate-500">Moi giao dich cong them 5 luot.</p>
              </div>
              <button
                type="button"
                className="grid h-9 w-9 place-items-center rounded-lg text-slate-500 transition hover:bg-slate-100 hover:text-slate-900 focus:outline-none focus:ring-2 focus:ring-emerald-500"
                onClick={() => setIsBuyModalOpen(false)}
                disabled={buyTurnsMutation.isPending}
                aria-label="Dong popup"
              >
                <X className="h-5 w-5" aria-hidden="true" />
              </button>
            </div>

            <div className="grid gap-3 p-5">
              <button
                type="button"
                className="flex items-center gap-4 rounded-lg border border-slate-200 p-4 text-left transition hover:border-sky-300 hover:bg-sky-50 focus:outline-none focus:ring-2 focus:ring-sky-500 disabled:cursor-not-allowed disabled:opacity-60"
                onClick={() => handleBuyTurns('VNPAY')}
                disabled={buyTurnsMutation.isPending}
              >
                <span className="grid h-11 w-11 shrink-0 place-items-center rounded-lg bg-sky-100 text-sky-700">
                  {buyTurnsMutation.isPending && buyTurnsMutation.variables?.provider === 'VNPAY' ? <Loader2 className="h-5 w-5 animate-spin" aria-hidden="true" /> : <CreditCard className="h-5 w-5" aria-hidden="true" />}
                </span>
                <span className="min-w-0">
                  <span className="block font-bold text-slate-950">VNPay</span>
                  <span className="block text-sm text-slate-600">Thanh toan qua cong VNPay sandbox.</span>
                </span>
              </button>

              <button
                type="button"
                className="flex items-center gap-4 rounded-lg border border-slate-200 p-4 text-left transition hover:border-emerald-300 hover:bg-emerald-50 focus:outline-none focus:ring-2 focus:ring-emerald-500 disabled:cursor-not-allowed disabled:opacity-60"
                onClick={() => handleBuyTurns('DEMO')}
                disabled={buyTurnsMutation.isPending}
              >
                <span className="grid h-11 w-11 shrink-0 place-items-center rounded-lg bg-emerald-100 text-emerald-700">
                  {buyTurnsMutation.isPending && buyTurnsMutation.variables?.provider === 'DEMO' ? <Loader2 className="h-5 w-5 animate-spin" aria-hidden="true" /> : <Wallet className="h-5 w-5" aria-hidden="true" />}
                </span>
                <span className="min-w-0">
                  <span className="block font-bold text-slate-950">Mua thuong</span>
                  <span className="block text-sm text-slate-600">Cong luot ngay bang thanh toan demo.</span>
                </span>
              </button>
            </div>
          </div>
        </div>
      ) : null}

      <section className="grid gap-6 lg:grid-cols-2">
        <div className="rounded-lg border border-slate-200 bg-white p-5">
          <div className="mb-4 flex items-center gap-2">
            <History className="h-5 w-5 text-emerald-700" aria-hidden="true" />
            <h2 className="text-lg font-bold">Lich su doan</h2>
          </div>
          {guessHistoryQuery.isLoading ? <LoadingSpinner /> : null}
          {guessHistoryQuery.isError ? <ErrorState message="Khong tai duoc lich su doan." onRetry={() => void guessHistoryQuery.refetch()} /> : null}
          {guessHistoryQuery.data && guessHistoryQuery.data.content.length === 0 ? <EmptyState title="Chua co luot doan" /> : null}
          {guessHistoryQuery.data && guessHistoryQuery.data.content.length > 0 ? (
            <>
              <div className="overflow-x-auto">
                <table className="min-w-full text-left text-sm">
                  <thead className="text-xs uppercase text-slate-500">
                    <tr>
                      <th className="py-2 pr-3">Ket qua</th>
                      <th className="py-2 pr-3">Ban</th>
                      <th className="py-2 pr-3">Server</th>
                      <th className="py-2 pr-3">Diem</th>
                      <th className="py-2">Thoi gian</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {guessHistoryQuery.data.content.map((item) => (
                      <tr key={item.id}>
                        <td className="py-3 pr-3 font-semibold">{item.result}</td>
                        <td className="py-3 pr-3">{item.guessedNumber}</td>
                        <td className="py-3 pr-3">{item.serverNumber}</td>
                        <td className="py-3 pr-3">{item.scoreAfter}</td>
                        <td className="py-3">{formatDateTime(item.createdAt)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <Pagination page={guessPage} totalPages={guessHistoryQuery.data.totalPages} onPageChange={setGuessPage} />
            </>
          ) : null}
        </div>

        <div className="rounded-lg border border-slate-200 bg-white p-5">
          <div className="mb-4 flex items-center gap-2">
            <RotateCw className="h-5 w-5 text-amber-700" aria-hidden="true" />
            <h2 className="text-lg font-bold">Lich su mua luot</h2>
          </div>
          {purchaseHistoryQuery.isLoading ? <LoadingSpinner /> : null}
          {purchaseHistoryQuery.isError ? <ErrorState message="Khong tai duoc lich su mua luot." onRetry={() => void purchaseHistoryQuery.refetch()} /> : null}
          {purchaseHistoryQuery.data && purchaseHistoryQuery.data.content.length === 0 ? <EmptyState title="Chua co giao dich" /> : null}
          {purchaseHistoryQuery.data && purchaseHistoryQuery.data.content.length > 0 ? (
            <>
              <div className="space-y-3">
                {purchaseHistoryQuery.data.content.map((item) => (
                  <div key={item.id} className="rounded-lg border border-slate-200 p-3 text-sm">
                    <div className="flex items-center justify-between gap-3">
                      <span className="font-semibold text-slate-950">+{item.turnsAdded} luot</span>
                      <span className="rounded-md bg-emerald-50 px-2 py-1 text-xs font-bold text-emerald-700">{item.provider}</span>
                    </div>
                    <p className="mt-2 text-slate-600">{item.transactionCode}</p>
                    <p className="mt-1 text-slate-600">
                      {item.status} - {formatCurrency(item.amount)} - {formatDateTime(item.createdAt)}
                    </p>
                  </div>
                ))}
              </div>
              <Pagination page={purchasePage} totalPages={purchaseHistoryQuery.data.totalPages} onPageChange={setPurchasePage} />
            </>
          ) : null}
        </div>
      </section>
    </div>
  );
}

interface LeaderboardPanelProps {
  isLoading: boolean;
  isError: boolean;
  onRetry: () => void;
  items: { rank: number; userId: number; username: string; score: number; turns: number }[];
}

function LeaderboardPanel({ isLoading, isError, onRetry, items }: LeaderboardPanelProps) {
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-5">
      <div className="mb-4 flex items-center gap-2">
        <Trophy className="h-5 w-5 text-amber-600" aria-hidden="true" />
        <h2 className="text-lg font-bold">Leaderboard</h2>
      </div>
      {isLoading ? <LoadingSpinner /> : null}
      {isError ? <ErrorState message="Khong tai duoc bang xep hang." onRetry={onRetry} /> : null}
      {!isLoading && !isError && items.length === 0 ? <EmptyState title="Chua co nguoi choi" /> : null}
      <div className="space-y-3">
        {items.map((item) => (
          <div key={item.userId} className="flex items-center justify-between rounded-lg border border-slate-200 px-3 py-3">
            <div className="flex items-center gap-3">
              <span className="grid h-9 w-9 place-items-center rounded-lg bg-slate-100 font-bold text-slate-700">#{item.rank}</span>
              <div>
                <p className="font-semibold">{item.username}</p>
                <p className="text-xs text-slate-500">{item.turns} luot con lai</p>
              </div>
            </div>
            <span className="font-bold text-emerald-700">{item.score}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
