import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation } from '@tanstack/react-query';
import { Loader2 } from 'lucide-react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import type { ReactNode } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { api } from '../api/endpoints';
import { getApiError } from '../api/axiosClient';
import { useAuthStore } from '../store/authStore';

const loginSchema = z.object({
  usernameOrEmail: z.string().min(1, 'Nhap username hoac email'),
  password: z.string().min(1, 'Nhap mat khau')
});

type LoginForm = z.infer<typeof loginSchema>;

export function LoginPage() {
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);
  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<LoginForm>({ resolver: zodResolver(loginSchema) });

  const mutation = useMutation({
    mutationFn: api.login,
    onSuccess: (data) => {
      setAuth(data);
      toast.success('Dang nhap thanh cong');
      navigate('/');
    },
    onError: (error) => toast.error(getApiError(error).message)
  });

  return (
    <AuthShell title="Dang nhap" subtitle="Tiep tuc van may voi cac con so">
      <form className="space-y-4" onSubmit={handleSubmit((values) => mutation.mutate(values))}>
        <Field label="Username hoac email" error={errors.usernameOrEmail?.message}>
          <input className="input" autoComplete="username" {...register('usernameOrEmail')} />
        </Field>
        <Field label="Mat khau" error={errors.password?.message}>
          <input className="input" type="password" autoComplete="current-password" {...register('password')} />
        </Field>
        <button className="primary-button w-full" disabled={mutation.isPending} type="submit">
          {mutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" /> : null}
          Dang nhap
        </button>
      </form>
      <p className="mt-5 text-center text-sm text-slate-600">
        Chua co tai khoan?{' '}
        <Link className="font-semibold text-emerald-700" to="/register">
          Dang ky
        </Link>
      </p>
    </AuthShell>
  );
}

interface AuthShellProps {
  title: string;
  subtitle: string;
  children: ReactNode;
}

export function AuthShell({ title, subtitle, children }: AuthShellProps) {
  return (
    <main className="grid min-h-screen place-items-center bg-slate-100 px-4 py-10">
      <section className="w-full max-w-md rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <div className="mb-6">
          <p className="text-sm font-semibold uppercase tracking-wide text-emerald-700">Guess Number Game</p>
          <h1 className="mt-2 text-2xl font-bold text-slate-950">{title}</h1>
          <p className="mt-1 text-sm text-slate-500">{subtitle}</p>
        </div>
        {children}
      </section>
    </main>
  );
}

interface FieldProps {
  label: string;
  error?: string;
  children: ReactNode;
}

export function Field({ label, error, children }: FieldProps) {
  return (
    <label className="block">
      <span className="text-sm font-medium text-slate-700">{label}</span>
      <span className="mt-1 block">{children}</span>
      {error ? <span className="mt-1 block text-sm text-rose-700">{error}</span> : null}
    </label>
  );
}
