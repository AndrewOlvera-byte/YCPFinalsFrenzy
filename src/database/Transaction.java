package database;

public interface Transaction<ResultType> {
    public ResultType execute(IDatabase db);
} 