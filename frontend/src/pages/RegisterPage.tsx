import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation } from '@tanstack/react-query';
import { Loader2 } from 'lucide-react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import { Link, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { getApiError } from '../api/axiosClient';
import { api } from '../api/endpoints';
import { useAuthStore } from '../store/authStore';
import { AuthShell, Field } from './LoginPage';

const registerSchema = z
  .object({
    username: z
      .string()
      .min(3, 'Username toi thieu 3 ky tu')
      .max(50, 'Username toi da 50 ky tu')
      .regex(/^[a-zA-Z0-9_]+$/, 'Chi dung chu, so va dau gach duoi'),
    email: z.string().email('Email khong hop le').max(120, 'Email toi da 120 ky tu'),
    password: z
      .string()
      .min(8, 'Mat khau toi thieu 8 ky tu')
      .max(100, 'Mat khau toi da 100 ky tu')
      .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/, 'Can chu hoa, chu thuong va so'),
    confirmPassword: z.string().min(1, 'Nhap lai mat khau')
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Mat khau nhap lai khong khop',
    path: ['confirmPassword']
  });

type RegisterForm = z.infer<typeof registerSchema>;

export function RegisterPage() {
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);
  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<RegisterForm>({ resolver: zodResolver(registerSchema) });

  const mutation = useMutation({
    mutationFn: api.register,
    onSuccess: (data) => {
      setAuth(data);
      toast.success('Dang ky thanh cong');
      navigate('/');
    },
    onError: (error) => toast.error(getApiError(error).message)
  });

  return (
    <AuthShell title="Dang ky" subtitle="Nhan 5 luot choi dau tien">
      <form
        className="space-y-4"
        onSubmit={handleSubmit(({ username, email, password }) => mutation.mutate({ username, email, password }))}
      >
        <Field label="Username" error={errors.username?.message}>
          <input className="input" autoComplete="username" {...register('username')} />
        </Field>
        <Field label="Email" error={errors.email?.message}>
          <input className="input" type="email" autoComplete="email" {...register('email')} />
        </Field>
        <Field label="Mat khau" error={errors.password?.message}>
          <input className="input" type="password" autoComplete="new-password" {...register('password')} />
        </Field>
        <Field label="Nhap lai mat khau" error={errors.confirmPassword?.message}>
          <input className="input" type="password" autoComplete="new-password" {...register('confirmPassword')} />
        </Field>
        <button className="primary-button w-full" disabled={mutation.isPending} type="submit">
          {mutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" /> : null}
          Tao tai khoan
        </button>
      </form>
      <p className="mt-5 text-center text-sm text-slate-600">
        Da co tai khoan?{' '}
        <Link className="font-semibold text-emerald-700" to="/login">
          Dang nhap
        </Link>
      </p>
    </AuthShell>
  );
}
