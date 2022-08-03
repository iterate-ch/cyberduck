namespace System.Threading
{
    public static class ReaderWriterLockSlimExtensions
    {
        public static ReadLock UseReadLock(this ReaderWriterLockSlim @this) => new(@this);

        public static UpgradeableReadLock UseUpgradeableReadLock(this ReaderWriterLockSlim @this) => new(@this);

        public static WriteLock UseWriteLock(this ReaderWriterLockSlim @this) => new(@this);

        public ref struct ReadLock
        {
            private readonly Action exit;

            internal ReadLock(ReaderWriterLockSlim @this)
            {
                @this.EnterReadLock();
                exit = @this.ExitReadLock;
            }

            public void Dispose() => exit?.Invoke();
        }

        public ref struct UpgradeableReadLock
        {
            private readonly Action exit;

            internal UpgradeableReadLock(ReaderWriterLockSlim @this)
            {
                @this.EnterUpgradeableReadLock();
                exit = @this.ExitUpgradeableReadLock;
            }

            public void Dispose() => exit?.Invoke();
        }

        public ref struct WriteLock
        {
            private readonly Action exit;

            internal WriteLock(ReaderWriterLockSlim @this)
            {
                @this.EnterWriteLock();
                exit = @this.ExitWriteLock;
            }

            public void Dispose() => exit?.Invoke();
        }
    }
}
