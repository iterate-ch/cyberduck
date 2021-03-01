using System;
using System.Runtime.CompilerServices;

namespace Ch.Cyberduck.Core.Microsoft.Windows.Sdk
{
    using static PInvoke;

    public abstract unsafe record HandleTrait<T> where T : unmanaged
    {
        public T* Invalid { get; }

        public HandleTrait(T* invalid)
        {
            Invalid = invalid;
        }

        public abstract void Close(T* value);
    }

    public unsafe record CredHandleTrait() : HandleTrait<CREDENTIALW>(default(CREDENTIALW*))
    {
        public override unsafe void Close(CREDENTIALW* value) => CredFree(value);
    }

    public unsafe record HICONHandleTrait() : HandleTrait<HICON>(default(HICON*))
    {
        public override unsafe void Close(HICON* value) => DestroyIcon(*value);
    }

    public unsafe record PIDLIST_ABSOLUTEHandleTrait() : HandleTrait<ITEMIDLIST>(default(ITEMIDLIST*))
    {
        public override unsafe void Close(ITEMIDLIST* value) => CoTaskMemFree(value);
    }

    public unsafe record Handle<TSelf, TTrait, T> : IDisposable
        where TSelf : Handle<TSelf, TTrait, T>
        where TTrait : HandleTrait<T>, new()
        where T : unmanaged
    {
        private static readonly TTrait trait;

        private bool disposed = false;
        private T* pointer = trait.Invalid;

        static Handle()
        {
            trait = new TTrait();
        }

        public T* Pointer => pointer;

        public IntPtr IntPtr => (IntPtr)pointer;

        public ref T Value => ref Unsafe.AsRef<T>(pointer);

        public ref T* Put() => ref pointer;

        public TSelf With(in T value)
        {
            Attach(value);
            return (TSelf)this;
        }

        public TSelf With(IntPtr ptr)
        {
            Attach(ptr);
            return (TSelf)this;
        }

        public TSelf With(T* value)
        {
            Attach(value);
            return (TSelf)this;
        }

        public void Attach(in T value)
        {
            pointer = (T*)Unsafe.AsPointer(ref Unsafe.AsRef(value));
        }

        public void Attach(IntPtr ptr)
        {
            pointer = (T*)ptr;
        }

        public void Attach(T* value)
        {
            pointer = value;
        }

        ~Handle()
        {
            Dispose(false);
        }

        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        protected void Dispose(bool disposing)
        {
            if (!disposed)
            {
                disposed = true;
                if (this)
                {
                    trait.Close(pointer);
                    pointer = trait.Invalid;
                }
            }
        }

        public static implicit operator bool(in Handle<TSelf, TTrait, T> handle) => handle is not null && handle.pointer != trait.Invalid;
    }

    public record CredHandle : Handle<CredHandle, CredHandleTrait, CREDENTIALW>;

    public record HICONHandle : Handle<HICONHandle, HICONHandleTrait, HICON>;

    public record PIDLIST_ABSOLUTEHandle : Handle<PIDLIST_ABSOLUTEHandle, PIDLIST_ABSOLUTEHandleTrait, ITEMIDLIST>;
}
